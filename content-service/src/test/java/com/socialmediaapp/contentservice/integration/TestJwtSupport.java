package com.socialmediaapp.contentservice.integration;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

final class TestJwtSupport {

    private static final String TEST_SECRET = "test-only-jwt-signing-secret-that-is-at-least-32-bytes-long-1234567890";

    private TestJwtSupport() {
    }

    static String accessTokenFor(Long userId, String username, String role) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(900)))
                .signWith(key)
                .compact();
    }
}
