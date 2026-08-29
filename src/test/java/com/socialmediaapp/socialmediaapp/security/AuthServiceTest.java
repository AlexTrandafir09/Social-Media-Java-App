package com.socialmediaapp.socialmediaapp.security;

import com.socialmediaapp.socialmediaapp.user.entity.Role;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, refreshTokenRepository, passwordEncoder, jwtService);
        user = User.builder().id(1L).username("alice").password("hashed").role(Role.USER).build();
    }

    @Test
    void login_returnsTokenPairWhenCredentialsAreValid() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass1234", "hashed")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshTokenValue()).thenReturn("raw-refresh-token");
        when(jwtService.hashToken("raw-refresh-token")).thenReturn("hashed-refresh-token");
        when(jwtService.refreshTokenExpiresAt()).thenReturn(Instant.now().plusSeconds(3600));

        AuthService.TokenPair result = authService.login("alice", "pass1234");

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("raw-refresh-token");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenHash()).isEqualTo("hashed-refresh-token");
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    void login_throwsWhenUsernameNotFound() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("nobody", "pass1234"))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void login_throwsWhenPasswordIsWrong() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("alice", "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refresh_rotatesTokenWhenValid() {
        RefreshToken existing = RefreshToken.builder()
                .id(10L)
                .tokenHash("hash-of-old")
                .user(user)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(jwtService.hashToken("old-raw-token")).thenReturn("hash-of-old");
        when(refreshTokenRepository.findByTokenHash("hash-of-old")).thenReturn(Optional.of(existing));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
        when(jwtService.generateRefreshTokenValue()).thenReturn("new-raw-token");
        when(jwtService.hashToken("new-raw-token")).thenReturn("hash-of-new");
        when(jwtService.refreshTokenExpiresAt()).thenReturn(Instant.now().plusSeconds(3600));

        AuthService.TokenPair result = authService.refresh("old-raw-token");

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-raw-token");
        verify(refreshTokenRepository).delete(existing);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refresh_throwsWhenTokenNotFound() {
        when(jwtService.hashToken("unknown")).thenReturn("hash-of-unknown");
        when(refreshTokenRepository.findByTokenHash("hash-of-unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("unknown"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void refresh_throwsAndDeletesWhenTokenExpired() {
        RefreshToken expired = RefreshToken.builder()
                .id(11L)
                .tokenHash("hash-of-expired")
                .user(user)
                .expiresAt(Instant.now().minusSeconds(60))
                .build();
        when(jwtService.hashToken("expired-token")).thenReturn("hash-of-expired");
        when(refreshTokenRepository.findByTokenHash("hash-of-expired")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.refresh("expired-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository).delete(expired);
    }

    @Test
    void logout_deletesMatchingRefreshToken() {
        RefreshToken existing = RefreshToken.builder().id(12L).tokenHash("hash-x").user(user).build();
        when(jwtService.hashToken("raw-x")).thenReturn("hash-x");
        when(refreshTokenRepository.findByTokenHash("hash-x")).thenReturn(Optional.of(existing));

        authService.logout("raw-x");

        verify(refreshTokenRepository).delete(existing);
    }

    @Test
    void logout_isNoOpWhenTokenNotFound() {
        when(jwtService.hashToken("raw-y")).thenReturn("hash-y");
        when(refreshTokenRepository.findByTokenHash("hash-y")).thenReturn(Optional.empty());

        authService.logout("raw-y");

        verify(refreshTokenRepository, never()).delete(any());
    }
}
