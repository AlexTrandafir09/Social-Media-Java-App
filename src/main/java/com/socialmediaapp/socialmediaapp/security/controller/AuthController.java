package com.socialmediaapp.socialmediaapp.security.controller;

import com.socialmediaapp.socialmediaapp.security.dto.AuthResponse;
import com.socialmediaapp.socialmediaapp.security.dto.LoginRequest;
import com.socialmediaapp.socialmediaapp.security.exception.InvalidRefreshTokenException;
import com.socialmediaapp.socialmediaapp.security.service.AuthService;
import com.socialmediaapp.socialmediaapp.security.service.JwtService;
import com.socialmediaapp.socialmediaapp.user.dto.UserRegistrationRequest;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .build();
        User created = userService.createUser(user);
        AuthService.TokenPair tokens = authService.issueTokens(created);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken()).toString())
                .body(new AuthResponse(tokens.accessToken(), "Bearer", jwtService.getAccessTokenExpirationSeconds()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.TokenPair tokens = authService.login(request.username(), request.password());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken()).toString())
                .body(new AuthResponse(tokens.accessToken(), "Bearer", jwtService.getAccessTokenExpirationSeconds()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken == null) {
            throw new InvalidRefreshTokenException();
        }
        AuthService.TokenPair tokens = authService.refresh(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken()).toString())
                .body(new AuthResponse(tokens.accessToken(), "Bearer", jwtService.getAccessTokenExpirationSeconds()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .build();
    }

    private ResponseCookie buildRefreshCookie(String value) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(jwtService.getRefreshTokenExpirationSeconds())
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(0)
                .build();
    }
}
