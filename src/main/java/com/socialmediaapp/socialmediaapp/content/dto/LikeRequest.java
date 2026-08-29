package com.socialmediaapp.socialmediaapp.content.dto;

import jakarta.validation.constraints.NotNull;

public record LikeRequest(
        @NotNull Long postId
) {
}
