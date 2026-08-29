package com.socialmediaapp.socialmediaapp.content.dto;

import com.socialmediaapp.socialmediaapp.content.entity.ImageFilter;
import jakarta.validation.constraints.NotBlank;

public record PostImageCreateRequest(
        @NotBlank String storageKey,
        ImageFilter filter
) {
}
