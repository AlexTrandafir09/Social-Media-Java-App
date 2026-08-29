package com.socialmediaapp.followservice.follow.dto;

import jakarta.validation.constraints.NotNull;

public record FollowRequest(
        @NotNull Long followingId
) {
}
