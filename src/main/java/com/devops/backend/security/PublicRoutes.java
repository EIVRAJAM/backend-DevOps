package com.devops.backend.security;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PublicRoutes {
    // Para SecurityConfig
    public static final String[] SECURITY_MATCHERS = {
            "/v1/auth/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    // Para JwtValidationFilter
    public static final String[] FILTER_PREFIXES = {
            "/api/v1/auth",
            "/api/swagger-ui",
            "/api/v3/api-docs"
    };
}
