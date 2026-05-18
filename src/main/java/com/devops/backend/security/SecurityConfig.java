package com.devops.backend.security;

import com.devops.backend.security.filter.JwtAuthenticationFilter;
import com.devops.backend.security.filter.JwtValidationFilter;
import com.devops.backend.auth.handler.OAuth2SuccessHandler;
import com.devops.backend.auth.handler.OAuth2FailureHandler;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad para la aplicación
 * 
 * Características:
 * - JWT para autenticación stateless
 * - OAuth2 con Google integrado
 * - CORS configurado para desarrollo y Azure
 * - Sesiones minimizadas (stateless con JWT)
 * - CSRF deshabilitado (API REST, no formularios)
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Autowired
    private OAuth2FailureHandler oAuth2FailureHandler;

    @Bean
    AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configuración CORS adaptable para desarrollo y Azure
     * 
     * En Azure, el frontend puede estar en:
     * - Static Web Apps (dominio.azurestaticapps.net)
     * - App Service (app-name.azurewebsites.net)
     * - Dominio personalizado
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
            "http://localhost:5173",
            "http://localhost:*",
            "https://devops-test-front.vercel.app",
            "https://*.vercel.app"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        AuthenticationManager authManager = authenticationManager();

        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authManager);
        JwtValidationFilter jwtValidationFilter = new JwtValidationFilter(authManager, objectMapper);

        return http
                // CORS: permitir orígenes configurados
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Autorización: rutas públicas vs protegidas
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PublicRoutes.SECURITY_MATCHERS).permitAll()
                        .anyRequest().authenticated())

                // Filters JWT (sin sesión)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtValidationFilter, UsernamePasswordAuthenticationFilter.class)

                // OAuth2 con Google
                // - successHandler: genera JWT y redirige
                // - failureHandler: maneja errores y redirige con error
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler))

                // CSRF deshabilitado (API REST, no formularios)
                .csrf(AbstractHttpConfigurer::disable)

                // Sesiones: IF_REQUIRED
                // - Genera sesión solo si es necesaria (OAuth2 puede requerir)
                // - JWT proporciona autenticación stateless
                // - Compatible con Azure App Service (sin sticky sessions)
                .sessionManagement(management -> management
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}