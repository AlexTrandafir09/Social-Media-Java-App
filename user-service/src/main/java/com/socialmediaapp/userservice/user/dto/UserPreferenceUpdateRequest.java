package com.socialmediaapp.userservice.user.dto;

public record UserPreferenceUpdateRequest(
        boolean notifyOnLike,
        boolean notifyOnComment,
        boolean notifyOnFollow
) {
}
