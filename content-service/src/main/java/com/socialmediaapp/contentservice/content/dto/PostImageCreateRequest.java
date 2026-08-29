package com.socialmediaapp.contentservice.content.dto;

import com.socialmediaapp.contentservice.content.entity.ImageFilter;
import jakarta.validation.constraints.NotBlank;

public record PostImageCreateRequest(
        @NotBlank String storageKey,
        ImageFilter filter
) {
}
