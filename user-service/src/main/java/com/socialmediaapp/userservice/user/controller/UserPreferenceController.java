package com.socialmediaapp.userservice.user.controller;

import com.socialmediaapp.userservice.security.SecurityUtils;
import com.socialmediaapp.userservice.user.dto.UserPreferenceUpdateRequest;
import com.socialmediaapp.userservice.user.entity.UserPreference;
import com.socialmediaapp.userservice.user.service.UserPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @GetMapping
    public UserPreference getPreferences(@PathVariable Long userId) {
        if (!SecurityUtils.isCurrentUserOrAdmin(userId)) {
            throw new AccessDeniedException("You can only view your own preferences");
        }
        return userPreferenceService.getByUserId(userId);
    }

    @PutMapping
    public UserPreference updatePreferences(@PathVariable Long userId, @Valid @RequestBody UserPreferenceUpdateRequest request) {
        return userPreferenceService.update(userId, request.notifyOnLike(), request.notifyOnComment(), request.notifyOnFollow());
    }
}
