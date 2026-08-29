package com.socialmediaapp.userservice.security.service;

import com.socialmediaapp.userservice.security.entity.RefreshToken;
import com.socialmediaapp.userservice.security.exception.InvalidCredentialsException;
import com.socialmediaapp.userservice.security.exception.InvalidRefreshTokenException;
import com.socialmediaapp.userservice.security.repository.RefreshTokenRepository;
import com.socialmediaapp.userservice.user.entity.User;
import com.socialmediaapp.userservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public record TokenPair(String accessToken, String refreshToken) {
    }

    public TokenPair login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        log.info("User logged in: id={}, username={}", user.getId(), user.getUsername());
        return issueTokens(user);
    }

    public TokenPair refresh(String rawRefreshToken) {
        RefreshToken existing = refreshTokenRepository.findByTokenHash(jwtService.hashToken(rawRefreshToken))
                .orElseThrow(InvalidRefreshTokenException::new);
        if (existing.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(existing);
            throw new InvalidRefreshTokenException();
        }
        User user = existing.getUser();
        refreshTokenRepository.delete(existing);
        log.debug("Refresh token rotated: userId={}", user.getId());
        return issueTokens(user);
    }

    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(jwtService.hashToken(rawRefreshToken))
                .ifPresent(refreshTokenRepository::delete);
    }

    public TokenPair issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = jwtService.generateRefreshTokenValue();
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(jwtService.hashToken(rawRefreshToken))
                .user(user)
                .expiresAt(jwtService.refreshTokenExpiresAt())
                .build();
        refreshTokenRepository.save(refreshToken);
        return new TokenPair(accessToken, rawRefreshToken);
    }
}
