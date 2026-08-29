package com.socialmediaapp.socialmediaapp.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostImageCreateRequest(
        @NotNull Long postId,
        @NotBlank String storageKey,
        ImageFilter filter
) {
}
