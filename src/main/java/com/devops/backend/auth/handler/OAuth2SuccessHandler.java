package com.devops.backend.auth.handler;

import com.devops.backend.auth.service.OAuthUserServiceImpl;
import com.devops.backend.sesion.entity.Sesion;
import com.devops.backend.sesion.repository.SesionRepository;
import com.devops.backend.security.TokenJwtConfig;
import com.devops.backend.usuario.entity.Usuario;
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
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manejador de autenticación exitosa OAuth2 con Google
 * 
 * Flujo:
 * 1. Google autentica al usuario
 * 2. Spring Security procesa callback OAuth2
 * 3. Este handler obtiene el usuario autenticado
 * 4. Busca o crea usuario local (OAuthUserServiceImpl)
 * 5. Genera JWT propio del sistema (no depende de Google)
 * 6. Registra sesión en tabla sesiones con tipo_login=GOOGLE
 * 7. Redirecciona al frontend sin exponer JWT en URL
 * 8. Frontend solicita JWT en segundo step via GET /api/v1/auth/oauth/success
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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        try {
            // Paso 1: Extraer OAuth2User desde Authentication
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            // Paso 2: Extraer atributos de Google
            String email = oAuth2User.getAttribute("email");
            String nombre = oAuth2User.getAttribute("given_name");
            String apellido = oAuth2User.getAttribute("family_name");

            if (email == null || email.isEmpty()) {
                log.error("Email no disponible en OAuth2User de Google");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email no disponible");
                return;
            }

            log.info("OAuth2 Google: procesando usuario {}", email);

            // Paso 3: Procesar usuario (buscar o crear)
            Usuario usuario = oAuthUserService.processOAuthUser(email, nombre, apellido);
            log.info("Usuario OAuth procesado: {} (ID: {})", usuario.getNombres(), usuario.getIdUsuario());

            // Paso 4: Generar JWT propio del sistema
            String jti = UUID.randomUUID().toString();
            long expirationMillis = jwtExpirationMinutes * 60 * 1000;

            List<Map<String, String>> authorities = usuario.getRol() != null
                    ? List.of(Map.of("authority", usuario.getRol().getNombreRol()))
                    : List.of(Map.of("authority", "ROLE_USER"));

            String token = Jwts.builder()
                    .subject(usuario.getIdUsuario().toString())
                    .claim("authorities", authorities)
                    .id(jti)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                    .signWith(TokenJwtConfig.SECRET_KEY)
                    .compact();

            log.info("JWT generado para usuario OAuth: {} (token: {}...)", usuario.getIdUsuario(),
                    token.substring(0, Math.min(20, token.length())));

            // Paso 5: Registrar sesión en BD con tipo_login=GOOGLE
            Sesion sesion = new Sesion();
            sesion.setUsuario(usuario);
            sesion.setFechaInicio(LocalDateTime.now());
            sesion.setActiva(true);
            sesion.setTokenJti(jti);
            sesion.setTipoLogin("GOOGLE"); // Marcar sesión como OAuth
            sesionRepository.save(sesion);

            log.info("Sesión registrada: {} (tipo_login=GOOGLE)", jti);

            // Paso 6: Redirigir al frontend sin exponer JWT en URL
            // El frontend sabrá que está autenticado y realizará una segunda llamada
            // a GET /api/v1/auth/oauth/success para obtener el JWT de forma segura
            String redirectUrl = frontendUrl + "?authenticated=true&provider=google";
            response.sendRedirect(redirectUrl);

        } catch (Exception ex) {
            log.error("Error en OAuth2SuccessHandler", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error en autenticación OAuth2");
        }
    }
}
