package com.socialmediaapp.socialmediaapp.security;

import com.socialmediaapp.socialmediaapp.user.entity.Role;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "test-signing-secret-that-is-at-least-32-bytes-long-1234567890",
                15,
                7
        );
    }

    @Test
    void generateAccessToken_encodesSubjectUsernameAndRole() {
        User user = User.builder().id(42L).username("alice").role(Role.ADMIN).build();

        String token = jwtService.generateAccessToken(user);
        Claims claims = jwtService.parseAccessToken(token);

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("username", String.class)).isEqualTo("alice");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void parseAccessToken_throwsWhenTokenIsTamperedOrInvalid() {
        assertThatThrownBy(() -> jwtService.parseAccessToken("not-a-real-token"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void parseAccessToken_throwsWhenSignedWithDifferentKey() {
        User user = User.builder().id(1L).username("bob").role(Role.USER).build();
        JwtService otherService = new JwtService(
                "a-completely-different-signing-secret-that-is-also-32-bytes-plus",
                15,
                7
        );
        String token = otherService.generateAccessToken(user);

        assertThatThrownBy(() -> jwtService.parseAccessToken(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void hashToken_isDeterministicAndProducesSha256Hex() {
        String hash1 = jwtService.hashToken("some-refresh-token-value");
        String hash2 = jwtService.hashToken("some-refresh-token-value");

        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).hasSize(64);
    }

    @Test
    void hashToken_producesDifferentHashesForDifferentInputs() {
        String hash1 = jwtService.hashToken("token-a");
        String hash2 = jwtService.hashToken("token-b");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void generateRefreshTokenValue_producesUniqueValues() {
        String first = jwtService.generateRefreshTokenValue();
        String second = jwtService.generateRefreshTokenValue();

        assertThat(first).isNotEqualTo(second);
        assertThat(first).isNotBlank();
    }
}
