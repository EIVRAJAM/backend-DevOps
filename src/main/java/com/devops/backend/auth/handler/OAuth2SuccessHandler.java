package com.devops.backend.auth.handler;

import com.devops.backend.auth.service.OAuthUserServiceImpl;
import com.devops.backend.sesion.entity.Sesion;
import com.devops.backend.sesion.repository.SesionRepository;
import com.devops.backend.security.TokenJwtConfig;
import com.devops.backend.usuario.entity.Usuario;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manejador de autenticación exitosa OAuth2 con Google
 * 
 * Flujo optimizado para Azure y local:
 * 1. Google autentica al usuario
 * 2. Spring Security procesa callback OAuth2
 * 3. Este handler extrae el OAuth2User (en memoria, no requiere sesión)
 * 4. Busca o crea usuario local (OAuthUserServiceImpl)
 * 5. Genera JWT propio del sistema (no depende de Google ni de sesión)
 * 6. Registra sesión en tabla sesiones con tipo_login=GOOGLE
 * 7. Redirecciona al frontend con JWT + metadata en query params (URL-encoded)
 * 8. Frontend lee token desde URL y lo guarda en localStorage/sessionStorage
 * 
 * Ventajas:
 * - No requiere mantener sesión OAuth2 en servidor
 * - Compatible con Azure App Service (sin sticky sessions)
 * - Token JWT autocontendido con todos los claims necesarios
 * - Fallbacks claros para cada etapa del flujo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthUserServiceImpl oAuthUserService;
    private final SesionRepository sesionRepository;

    @Value("${oauth2.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${jwt.expiration-minutes:30}")
    private long jwtExpirationMinutes;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        if (response.isCommitted()) {
            log.warn("OAuth2SuccessHandler: respuesta ya comprometida, abortando");
            return;
        }

        try {
            // Paso 1: Validar que existe el principal y es OAuth2User
            if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
                log.error("OAuth2SuccessHandler: Authentication o OAuth2User no válido");
                redirectWithError(response, "invalid_auth");
                return;
            }

            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            // Paso 2: Extraer y validar atributos de Google (en memoria, no de sesión)
            String email = extractAndValidateEmail(oAuth2User);
            if (email == null) {
                redirectWithError(response, "email_not_found");
                return;
            }

            String nombre = oAuth2User.getAttribute("given_name");
            String apellido = oAuth2User.getAttribute("family_name");

            log.debug("OAuth2 Google: iniciando procesamiento para email: {}", maskEmail(email));

            // Paso 3: Procesar usuario (buscar o crear) - transaccional
            Usuario usuario;
            try {
                usuario = oAuthUserService.processOAuthUser(email, nombre, apellido);
            } catch (IllegalStateException e) {
                log.warn("Cuenta bloqueada en OAuth2: {}", maskEmail(email));
                redirectWithError(response, "account_locked");
                return;
            } catch (Exception e) {
                log.error("Error procesando usuario OAuth: {}", e.getMessage(), e);
                redirectWithError(response, "user_processing_error");
                return;
            }

            if (usuario == null || usuario.getIdUsuario() == null) {
                log.error("Usuario procesado es null");
                redirectWithError(response, "user_processing_error");
                return;
            }

            log.info("Usuario OAuth procesado exitosamente: ID={}, nombre={}", usuario.getIdUsuario(),
                    usuario.getNombres());

            // Paso 4: Generar JWT propio del sistema
            String token;
            String jti;
            try {
                jti = UUID.randomUUID().toString();
                token = generateJWT(usuario, jti);
            } catch (JwtException e) {
                log.error("Error generando JWT: {}", e.getMessage(), e);
                redirectWithError(response, "jwt_generation_error");
                return;
            }

            log.debug("JWT generado exitosamente: JTI={}, usuario={}", jti, usuario.getIdUsuario());

            // Paso 5: Registrar sesión en BD - con fallback silencioso
            try {
                registerSession(usuario, jti);
            } catch (Exception e) {
                log.warn("Advertencia: no se pudo registrar sesión en BD: {}. Continuando con redirect.",
                        e.getMessage());
                // No bloqueamos el flujo si falla la sesión
            }

            // Paso 6: Redirigir al frontend con JWT + metadata
            redirectWithSuccess(response, token, usuario, jti);

        } catch (Exception ex) {
            log.error("Error inesperado en OAuth2SuccessHandler: {}", ex.getMessage(), ex);
            try {
                redirectWithError(response, "server_error");
            } catch (IOException ioEx) {
                log.error("Error durante redirect de error: {}", ioEx.getMessage());
            }
        }
    }

    /**
     * Extrae y valida el email del OAuth2User
     */
    private String extractAndValidateEmail(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            log.error("OAuth2: Email no disponible en OAuth2User");
            return null;
        }

        email = email.trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            log.error("OAuth2: Email inválido o vacío: {}", maskEmail(email));
            return null;
        }

        return email;
    }

    /**
     * Valida formato de email
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    /**
     * Genera JWT con todos los claims necesarios
     */
    private String generateJWT(Usuario usuario, String jti) throws JwtException {
        long expirationMillis = jwtExpirationMinutes * 60 * 1000;

        List<Map<String, String>> authorities = usuario.getRol() != null
                ? List.of(Map.of("authority", usuario.getRol().getNombreRol()))
                : List.of(Map.of("authority", "ROLE_USER"));

        return Jwts.builder()
                .subject(usuario.getIdUsuario().toString())
                .claim("authorities", authorities)
                .claim("email", usuario.getAcceso() != null ? usuario.getAcceso().getCorreoAcceso() : "")
                .claim("nombre", usuario.getNombres())
                .claim("apellido", usuario.getApellidos())
                .claim("authProvider", "GOOGLE")
                .id(jti)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(TokenJwtConfig.SECRET_KEY)
                .compact();
    }

    /**
     * Registra la sesión en BD
     */
    private void registerSession(Usuario usuario, String jti) {
        Sesion sesion = new Sesion();
        sesion.setUsuario(usuario);
        sesion.setFechaInicio(LocalDateTime.now());
        sesion.setActiva(true);
        sesion.setTokenJti(jti);
        sesion.setTipoLogin("GOOGLE");
        sesionRepository.save(sesion);
        log.debug("Sesión registrada: JTI={}, usuarioId={}", jti, usuario.getIdUsuario());
    }

    /**
     * Redirige exitosamente con token (URL-encoded)
     */
    private void redirectWithSuccess(HttpServletResponse response, String token, Usuario usuario, String jti)
            throws IOException {

        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        String redirectUrl = String.format("%s/oauth2/success?token=%s&userId=%d&provider=google",
                frontendUrl,
                encodedToken,
                usuario.getIdUsuario());

        log.info("OAuth2 redirect exitoso: usuarioId={}, jti={}, destino={}", usuario.getIdUsuario(), jti, frontendUrl);
        response.sendRedirect(redirectUrl);
    }

    /**
     * Redirige con error (incluye motivo para debugging)
     */
    private void redirectWithError(HttpServletResponse response, String reason) throws IOException {
        String redirectUrl = frontendUrl + "/oauth2/error?reason=" + reason + "&timestamp="
                + System.currentTimeMillis();
        log.warn("OAuth2 redirect con error: reason={}", reason);
        response.sendRedirect(redirectUrl);
    }

    /**
     * Enmascara email para logs
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 5)
            return "***";
        int atIndex = email.indexOf('@');
        if (atIndex < 2)
            return "***";
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}