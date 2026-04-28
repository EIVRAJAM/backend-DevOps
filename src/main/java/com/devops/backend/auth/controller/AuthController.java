package com.devops.backend.auth.controller;

import com.devops.backend.auth.dto.*;
import com.devops.backend.auth.service.AuthService;
import com.devops.backend.auth.service.PasswordResetService;
import com.devops.backend.auth.service.AccountUnlockService;
import com.devops.backend.exception.ApiValidationError;
import com.devops.backend.exception.ValidationException;
import com.devops.backend.sesion.entity.Sesion;
import com.devops.backend.sesion.repository.SesionRepository;
import com.devops.backend.security.TokenJwtConfig;
import com.devops.backend.usuario.entity.Usuario;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@Tag(name = "Authentication", description = "Operaciones de autenticación y recuperación de credenciales. Incluye registro, inicio/cierre de sesión, recuperación de contraseña y desbloqueo de cuentas.")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private AccountUnlockService accountUnlockService;

    @Autowired
    private Validator validator;

    @Autowired
    private SesionRepository sesionRepository;

    @PostMapping("/signup")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una nueva cuenta de usuario en el sistema. Se valida que el documento y username sean únicos. El usuario debe proporcionarse con todos los datos personales y credenciales requeridas.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente. Retorna datos del usuario creado."),
            @ApiResponse(responseCode = "400", description = "Validación fallida: campos incompletos, formato inválido, o documento/username ya existen."),
            @ApiResponse(responseCode = "409", description = "Conflicto: documento o username ya están registrados en el sistema.")
    })
    public ResponseEntity<?> signup(@RequestBody SignUpRequest signUpRequest) {
        System.out.println("aqui andamos");

        List<ApiValidationError> errors = new ArrayList<>();

        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(signUpRequest);
        for (ConstraintViolation<SignUpRequest> violation : violations) {
            String field = violation.getPropertyPath().toString();
            errors.add(new ApiValidationError(field, violation.getMessage()));
        }

        errors.addAll(authService.validateSignup(signUpRequest));

        if (!errors.isEmpty()) {
            throw new ValidationException("Errores en el registro", errors);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(authService.save(signUpRequest));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario con correo/username y contraseña. Devuelve un token JWT válido por 24 horas. El token debe incluirse en el header 'Authorization: Bearer {token}' en las siguientes peticiones.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa. Retorna {\"token\": \"eyJhbGciOiJIUzI1NiIs...\"}"),
            @ApiResponse(responseCode = "400", description = "Validación fallida: correo/username o contraseña vacíos o mal formateados."),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas: usuario no existe o contraseña incorrecta."),
            @ApiResponse(responseCode = "429", description = "Demasiados intentos fallidos: cuenta bloqueada temporalmente por seguridad.")
    })
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        try {
            Map<String, String> response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Invalida el token JWT actual agregándolo a la blacklist. El usuario no podrá usar este token en futuras peticiones. Requiere el token en el header 'Authorization: Bearer {token}'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente. Token agregado a blacklist."),
            @ApiResponse(responseCode = "400", description = "Error: Token no proporcionado o formato inválido del header Authorization."),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado.")
    })
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token no proporcionado"));
        }

        String token = header.replace("Bearer ", "").trim();
        authService.logout(token);

        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperación de contraseña", description = "Inicia el proceso de recuperación de contraseña. Si el correo está registrado, se envía un código de verificación de 6 dígitos. Este código es válido por 15 minutos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud procesada. Si el correo existe, se envía código de verificación."),
            @ApiResponse(responseCode = "400", description = "Validación fallida: correo vacío o formato inválido."),
            @ApiResponse(responseCode = "500", description = "Error interno: problema al enviar el correo de recuperación.")
    })
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        try {
            passwordResetService.requestPasswordReset(request);
            return ResponseEntity.ok(new PasswordResetResponse(
                    "Si el correo está registrado, recibirás un código de verificación",
                    true));
        } catch (com.devops.backend.exception.BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PasswordResetResponse(
                            e.getMessage(),
                            false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PasswordResetResponse(
                            "Error al procesar la solicitud",
                            false));
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Resetear contraseña", description = "Completa el proceso de recuperación de contraseña usando el código verificado. La nueva contraseña debe tener mínimo 8 caracteres, incluir mayúsculas, minúsculas y números.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente. Puede iniciar sesión con la nueva contraseña."),
            @ApiResponse(responseCode = "400", description = "Validación fallida: código inválido/expirado, contraseña no cumple requisitos, o confirmación no coincide."),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado para el correo proporcionado."),
            @ApiResponse(responseCode = "500", description = "Error interno al procesar el reseteo de contraseña.")
    })
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request);
            return ResponseEntity.ok(new PasswordResetResponse(
                    "Contraseña actualizada exitosamente",
                    true));
        } catch (com.devops.backend.exception.BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PasswordResetResponse(
                            e.getMessage(),
                            false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PasswordResetResponse(
                            "Error al resetear la contraseña",
                            false));
        }
    }

    @PostMapping("/request-account-unlock")
    @Operation(summary = "Solicitar desbloqueo de cuenta", description = "Inicia el proceso de desbloqueo si la cuenta está bloqueada por múltiples intentos de login fallidos. Se envía un código de verificación al correo registrado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud procesada. Si la cuenta está bloqueada, se envía código de desbloqueo."),
            @ApiResponse(responseCode = "400", description = "Validación fallida: correo vacío o formato inválido."),
            @ApiResponse(responseCode = "500", description = "Error interno: problema al enviar el código de desbloqueo.")
    })
    public ResponseEntity<?> requestAccountUnlock(@RequestBody @Valid ForgotPasswordRequest request) {
        try {
            accountUnlockService.requestAccountUnlock(request.email());
            return ResponseEntity.ok(new PasswordResetResponse(
                    "Si la cuenta está bloqueada, recibirás un código de verificación en tu correo",
                    true));
        } catch (com.devops.backend.exception.BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PasswordResetResponse(
                            e.getMessage(),
                            false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PasswordResetResponse(
                            "Error al procesar la solicitud de desbloqueo",
                            false));
        }
    }

    @PostMapping("/unlock-account")
    @Operation(summary = "Desbloquear cuenta", description = "Desbloquea una cuenta que fue bloqueada por múltiples intentos fallidos de login. Requiere el código de verificación enviado al correo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta desbloqueada exitosamente. Ahora puede iniciar sesión nuevamente."),
            @ApiResponse(responseCode = "400", description = "Validación fallida: código inválido, expirado, o cuenta no está bloqueada."),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado para el correo proporcionado."),
            @ApiResponse(responseCode = "500", description = "Error interno al procesar el desbloqueo.")
    })
    public ResponseEntity<?> unlockAccount(@RequestBody @Valid UnlockAccountRequest request) {
        try {
            accountUnlockService.unlockAccount(request);
            return ResponseEntity.ok(new PasswordResetResponse(
                    "Cuenta desbloqueada exitosamente",
                    true));
        } catch (com.devops.backend.exception.BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PasswordResetResponse(
                            e.getMessage(),
                            false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PasswordResetResponse(
                            "Error al desbloquear la cuenta",
                            false));
        }
    }

    @GetMapping("/oauth/success")
    @Operation(summary = "Obtener JWT después de autenticación OAuth2", description = "Endpoint seguro para obtener el JWT del sistema tras autenticarse con Google OAuth2. "
            +
            "El frontend redirige aquí después del callback OAuth2. Devuelve el token y datos del usuario. " +
            "Este endpoint REQUIERE estar autenticado (Spring Security establece contexto tras OAuth2 callback).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT obtenido exitosamente. Retorna token y datos del usuario."),
            @ApiResponse(responseCode = "401", description = "No autenticado. Usuario no tiene sesión válida de OAuth2."),
            @ApiResponse(responseCode = "500", description = "Error interno al obtener el JWT.")
    })
    public ResponseEntity<?> getOAuthSuccess() {
        try {
            // Obtener usuario autenticado del SecurityContext (establecido por
            // OAuth2SuccessHandler)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("OAuth2 success endpoint: usuario no autenticado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "No autenticado", "message", "No hay sesión OAuth activa"));
            }

            // Extraer email del OAuth2User (verificado por Google)
            final String email;
            Object principal = authentication.getPrincipal();

            if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
                org.springframework.security.oauth2.core.user.OAuth2User oAuth2User = (org.springframework.security.oauth2.core.user.OAuth2User) principal;
                email = (String) oAuth2User.getAttribute("email");
            } else {
                email = null;
            }

            if (email == null || email.isEmpty()) {
                log.warn("OAuth2 success endpoint: email no disponible en OAuth2User");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "No autenticado", "message", "Email no disponible en OAuth2"));
            }

            log.info("OAuth2 success: buscando usuario por email {}", email);

            // Buscar usuario por correo (verificado por Google)
            Optional<Usuario> usuarioOpt = sesionRepository
                    .findAllByTipoLoginOrderByFechaInicioDesc("GOOGLE")
                    .stream()
                    .filter(sesion -> sesion.getUsuario() != null &&
                            sesion.getUsuario().getAcceso() != null &&
                            email.equalsIgnoreCase(sesion.getUsuario().getAcceso().getCorreoAcceso()) &&
                            sesion.getActiva())
                    .map(Sesion::getUsuario)
                    .findFirst();

            if (usuarioOpt.isEmpty()) {
                log.warn("OAuth2 success endpoint: usuario no encontrado para email {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "No autenticado", "message", "Usuario no encontrado"));
            }

            Usuario usuario = usuarioOpt.get();
            log.info("Usuario encontrado: {} (ID: {})", usuario.getNombres(), usuario.getIdUsuario());

            // Buscar última sesión GOOGLE activa para obtener JTI
            Optional<Sesion> lastOAuthSession = sesionRepository
                    .findAllByUsuarioIdUsuarioOrderByFechaInicioDesc(usuario.getIdUsuario())
                    .stream()
                    .filter(s -> "GOOGLE".equals(s.getTipoLogin()) && s.getActiva())
                    .findFirst();

            if (lastOAuthSession.isEmpty()) {
                log.warn("OAuth2 success endpoint: no hay sesión GOOGLE activa para usuario {}",
                        usuario.getIdUsuario());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "No autenticado", "message", "No hay sesión OAuth activa"));
            }

            Sesion sesion = lastOAuthSession.get();
            String jti = sesion.getTokenJti();

            // Generar JWT usando nuestro idUsuario del backend
            long jwtExpirationMillis = 30 * 60 * 1000; // 30 minutos default
            List<Map<String, String>> authorities = usuario.getRol() != null
                    ? List.of(Map.of("authority", usuario.getRol().getNombreRol()))
                    : List.of(Map.of("authority", "ROLE_USER"));

            String token = Jwts.builder()
                    .subject(usuario.getIdUsuario().toString())
                    .claim("authorities", authorities)
                    .id(jti)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + jwtExpirationMillis))
                    .signWith(TokenJwtConfig.SECRET_KEY)
                    .compact();

            log.info("JWT obtenido exitosamente para usuario OAuth: {} (email: {})", usuario.getIdUsuario(), email);

            // Extraer roles del usuario
            List<String> roles = usuario.getRol() != null
                    ? Collections.singletonList("ROLE_USER")
                    : Collections.emptyList();

            OAuth2SuccessResponse response = new OAuth2SuccessResponse(
                    token,
                    usuario.getAcceso() != null ? usuario.getAcceso().getUsername() : "user",
                    usuario.getAcceso() != null ? usuario.getAcceso().getCorreoAcceso() : "",
                    usuario.getIdUsuario(),
                    roles);

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Error en OAuth2 success endpoint", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno", "message", ex.getMessage()));
        }
    }

}