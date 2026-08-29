package com.socialmediaapp.contentservice.content.dto;

import com.socialmediaapp.contentservice.content.entity.ImageFilter;
import jakarta.validation.constraints.NotNull;

public record PostImageUpdateRequest(
        @NotNull ImageFilter filter
) {
}
