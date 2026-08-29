package com.socialmediaapp.socialmediaapp.content;

import jakarta.validation.constraints.NotBlank;

public record PostImageInput(
        @NotBlank String storageKey,
        ImageFilter filter
) {
}
