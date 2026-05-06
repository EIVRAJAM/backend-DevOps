package com.devops.backend.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador personalizado para fallos en autenticación OAuth2
 * 
 * Gestiona diferentes tipos de errores:
 * - Errores de Google (user_cancelled_login, invalid_scope, etc.)
 * - Errores de red/servidor
 * - Errores de validación
 * 
 * Redirige al frontend con código de error y motivo para UX mejorada
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${oauth2.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        if (response.isCommitted()) {
            log.warn("OAuth2FailureHandler: respuesta ya comprometida");
            return;
        }

        try {
            // Mapear excepción a código de error legible
            String errorCode = mapExceptionToErrorCode(exception);
            String errorDescription = mapExceptionToDescription(exception);

            log.warn("OAuth2 autenticación fallida: error={}, description={}, exception={}",
                    errorCode, errorDescription, exception.getClass().getSimpleName());

            // Redirigir con parámetros de error
            String redirectUrl = buildErrorRedirectUrl(errorCode, errorDescription);
            response.sendRedirect(redirectUrl);

        } catch (Exception ex) {
            log.error("Error en OAuth2FailureHandler: {}", ex.getMessage(), ex);
            try {
                // Fallback: redirigir con error genérico
                String fallbackUrl = frontendUrl + "/oauth2/error?reason=server_error";
                response.sendRedirect(fallbackUrl);
            } catch (IOException ioEx) {
                log.error("Fallo al hacer fallback redirect: {}", ioEx.getMessage());
            }
        }
    }

    /**
     * Mapea la excepción a un código de error
     */
    private String mapExceptionToErrorCode(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
            OAuth2Error error = oauth2Exception.getError();

            if (error != null && error.getErrorCode() != null) {
                String errorCode = error.getErrorCode();

                // Mapear errores comunes de OAuth2
                switch (errorCode) {
                    case "invalid_request":
                        return "invalid_request";
                    case "unauthorized_client":
                        return "unauthorized_client";
                    case "access_denied":
                    case "user_cancelled_login":
                        return "user_cancelled_login";
                    case "unsupported_response_type":
                        return "unsupported_response_type";
                    case "invalid_scope":
                        return "invalid_scope";
                    case "server_error":
                    case "temporarily_unavailable":
                        return "oauth_provider_error";
                    default:
                        return "oauth_error";
                }
            }
        }

        String exceptionMessage = exception.getMessage() != null ? exception.getMessage().toLowerCase() : "";

        // Mapear por mensaje de excepción
        if (exceptionMessage.contains("network")) {
            return "network_error";
        } else if (exceptionMessage.contains("timeout")) {
            return "timeout_error";
        } else if (exceptionMessage.contains("invalid")) {
            return "invalid_credentials";
        }

        return "authentication_failed";
    }

    /**
     * Mapea la excepción a una descripción legible para el usuario
     */
    private String mapExceptionToDescription(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
            OAuth2Error error = oauth2Exception.getError();

            if (error != null) {
                String errorCode = error.getErrorCode();

                switch (errorCode) {
                    case "access_denied":
                    case "user_cancelled_login":
                        return "Login cancelado. No se permitió el acceso a la información de Google.";
                    case "invalid_request":
                        return "Solicitud OAuth2 inválida. Verifica la configuración.";
                    case "invalid_scope":
                        return "Scopes inválidos. Contacta con administración.";
                    case "server_error":
                    case "temporarily_unavailable":
                        return "Servicio de Google no disponible. Intenta más tarde.";
                    default:
                        if (error.getDescription() != null) {
                            return error.getDescription();
                        }
                        return "Error en autenticación OAuth2. Intenta de nuevo.";
                }
            }
        }

        String exceptionMessage = exception.getMessage() != null ? exception.getMessage() : "";

        if (exceptionMessage.contains("network")) {
            return "Error de conexión. Verifica tu conexión a Internet.";
        } else if (exceptionMessage.contains("timeout")) {
            return "Timeout en conexión con Google. Intenta de nuevo.";
        } else if (exceptionMessage.contains("invalid")) {
            return "Credenciales inválidas.";
        }

        return "Error en autenticación. Intenta de nuevo más tarde.";
    }

    /**
     * Construye la URL de redirección con parámetros de error
     */
    private String buildErrorRedirectUrl(String errorCode, String errorDescription)
            throws UnsupportedEncodingException {
        String encodedDescription = URLEncoder.encode(errorDescription, StandardCharsets.UTF_8);
        return String.format("%s/oauth2/error?reason=%s&message=%s&timestamp=%d",
                frontendUrl,
                errorCode,
                encodedDescription,
                System.currentTimeMillis());
    }
}
