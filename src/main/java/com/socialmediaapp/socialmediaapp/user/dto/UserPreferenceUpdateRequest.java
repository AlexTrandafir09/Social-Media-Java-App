package com.socialmediaapp.socialmediaapp.user.dto;

public record UserPreferenceUpdateRequest(
        boolean notifyOnLike,
        boolean notifyOnComment,
        boolean notifyOnFollow
) {
}
