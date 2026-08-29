package com.socialmediaapp.socialmediaapp.user.controller;

import com.socialmediaapp.socialmediaapp.user.dto.UserPreferenceUpdateRequest;
import com.socialmediaapp.socialmediaapp.user.entity.UserPreference;
import com.socialmediaapp.socialmediaapp.user.service.UserPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @GetMapping
    public UserPreference getPreferences(@PathVariable Long userId) {
        return userPreferenceService.getByUserId(userId);
    }

    @PutMapping
    public UserPreference updatePreferences(@PathVariable Long userId, @Valid @RequestBody UserPreferenceUpdateRequest request) {
        return userPreferenceService.update(userId, request.notifyOnLike(), request.notifyOnComment(), request.notifyOnFollow());
    }
}
