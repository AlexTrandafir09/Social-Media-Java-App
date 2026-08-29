package com.socialmediaapp.socialmediaapp.content.dto;

import com.socialmediaapp.socialmediaapp.content.entity.ImageFilter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostImageCreateRequest(
        @NotNull Long postId,
        @NotBlank String storageKey,
        ImageFilter filter
) {
}
