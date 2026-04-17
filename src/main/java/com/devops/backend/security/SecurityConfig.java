package com.devops.backend.security;

import com.devops.backend.security.filter.JwtAuthenticationFilter;
import com.devops.backend.security.filter.JwtValidationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    @Bean
    AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager());
        jwtAuthenticationFilter.setFilterProcessesUrl("/api/auth/sign-in");

        JwtValidationFilter jwtValidationFilter = new JwtValidationFilter(authenticationManager());

        return http
                .authorizeHttpRequests(auth -> auth
                        // .requestMatchers(HttpMethod.GET, "/api/acceso/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/auth/**").permitAll() // Cualquier cosa en
                                                                                     // /api/auth/
                        .requestMatchers(HttpMethod.POST, "/v1/accesos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/accesos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/usuarios/**").permitAll() // Cualquier POST en
                                                                                         // /api/usuarios/
                        .requestMatchers(HttpMethod.GET, "/v1/usuarios/*/sesiones").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/usuarios/**").permitAll() // Cualquier GET en
                                                                                        // /api/usuarios/
                        .requestMatchers(HttpMethod.PUT, "/v1/usuarios/**").permitAll() // Cualquier GET en
                                                                                        // /api/usuarios/
                        .requestMatchers(HttpMethod.PATCH, "/v1/usuarios/**").permitAll() // Cualquier GET en
                                                                                          // /api/usuarios/
                        .anyRequest().authenticated())
                .addFilter(jwtAuthenticationFilter)
                .addFilter(jwtValidationFilter)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(management -> management
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
