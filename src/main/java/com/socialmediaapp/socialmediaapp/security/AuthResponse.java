package com.socialmediaapp.socialmediaapp.security;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
}
