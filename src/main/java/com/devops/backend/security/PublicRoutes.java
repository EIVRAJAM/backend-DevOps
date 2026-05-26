package com.devops.backend.security;

/**
 * Rutas públicas que no requieren autenticación JWT.
 * Utilizada por SecurityConfig para autorizar endpoints públicos.
 */
public final class PublicRoutes {

        private PublicRoutes() {
                // Clase de utilidad: no permitir instanciación
        }

        // Para SecurityConfig
        public static final String[] SECURITY_MATCHERS = {
                        "/api/v1/auth/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/oauth2/**", // Rutas de redirección OAuth2 a Google
                        "/login/oauth2/**", // Callback de OAuth2 desde Google
                        "/login/oauth2/code/google", // Callback específico para Google (sin /api prefix)
                        "/api/v1/stripe/**" // Webhooks de Stripe
        };

        // Para JwtValidationFilter
        public static final String[] FILTER_PREFIXES = {
                        "/api/v1/auth",
                        "/swagger-ui",
                        "/v3/api-docs",
                        "/oauth2", // Rutas OAuth2 públicas (sin /api prefix)
                        "/login/oauth2", // Callback OAuth2 público (sin /api prefix)
                        "/login/oauth2/code/google", // Callback específico para Google (sin /api prefix)
                        "/api/v1/stripe" // Prefijo para Webhooks de Stripe
        };
}
