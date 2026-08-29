package com.socialmediaapp.socialmediaapp.security.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
}
