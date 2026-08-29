package com.socialmediaapp.socialmediaapp.user.dto;

import jakarta.validation.constraints.NotNull;

public record FollowRequest(
        @NotNull Long followingId
) {
}
