package com.devops.backend.auth.dto;

public record JwtResponse(
        String token,
        String type,
        String username,
        String rol
) {
}
