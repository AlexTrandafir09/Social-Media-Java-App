package com.socialmediaapp.socialmediaapp.user.controller;

import com.socialmediaapp.socialmediaapp.security.SecurityUtils;
import com.socialmediaapp.socialmediaapp.user.dto.UserPreferenceUpdateRequest;
import com.socialmediaapp.socialmediaapp.user.entity.UserPreference;
import com.socialmediaapp.socialmediaapp.user.service.UserPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    // Ownership check lives here rather than in UserPreferenceService.getByUserId,
    // because that method also has a legitimate internal caller (NotificationService.notifyIfEnabled)
    // that must not be blocked by the HTTP-request-only authorization check.
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
