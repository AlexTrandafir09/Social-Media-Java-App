package com.socialmediaapp.userservice.user.controller;

import com.socialmediaapp.userservice.user.dto.ChangeEmailRequest;
import com.socialmediaapp.userservice.user.dto.ChangePasswordRequest;
import com.socialmediaapp.userservice.user.dto.UserUpdateRequest;
import com.socialmediaapp.userservice.user.entity.User;
import com.socialmediaapp.userservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        User updates = User.builder()
                .bio(request.bio())
                .avatarUrl(request.avatarUrl())
                .build();
        return userService.updateUser(id, updates);
    }

    @PatchMapping("/{id}/email")
    public User changeEmail(@PathVariable Long id, @Valid @RequestBody ChangeEmailRequest request) {
        return userService.changeEmail(id, request.newEmail());
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
