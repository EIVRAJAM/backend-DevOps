package com.devops.backend.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.devops.backend.security.PublicRoutes;

import static com.devops.backend.security.TokenJwtConfig.*;

import java.io.IOException;
import java.util.*;

public class JwtValidationFilter extends BasicAuthenticationFilter {

    private final ObjectMapper objectMapper;

    public JwtValidationFilter(AuthenticationManager authenticationManager, ObjectMapper objectMapper) {
        super(authenticationManager);
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String authHeader = request.getHeader(HEADER_AUTH);

        String path = request.getRequestURI();
        // System.out.println("PATH = " + path);
        // System.out.println("PATH = " + path.startsWith("/api/v1/auth"));
        if (isPublicRoute(path)) {
            chain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith(PREFIX_TOKEN)) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.replace(PREFIX_TOKEN, "").trim();

        try {
            Claims claims = Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload();
            String correoAcceso = claims.getSubject();

            @SuppressWarnings("unchecked")
            List<Map<String, String>> authorityList = claims.get("authorities", List.class);

            Collection<? extends GrantedAuthority> authorities = authorityList
                    .stream()
                    .map(m -> new SimpleGrantedAuthority(m.get("authority")))
                    .toList();

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(correoAcceso, null,
                    authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);
            chain.doFilter(request, response);

        } catch (JwtException e) {
            Map<String, String> body = new HashMap<>();

            body.put("error", e.getMessage());
            body.put("message", "El token JWT no es valido!");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(CONTENT_TYPE);
            response.getWriter().write(objectMapper.writeValueAsString(body));

            return;
        }
    }

    private boolean isPublicRoute(String path) {
        return Arrays.stream(PublicRoutes.FILTER_PREFIXES)
                .anyMatch(path::startsWith);
    }
}
