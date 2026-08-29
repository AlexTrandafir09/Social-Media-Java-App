package com.socialmediaapp.socialmediaapp.content.dto;

import com.socialmediaapp.socialmediaapp.content.entity.ImageFilter;
import jakarta.validation.constraints.NotNull;

public record PostImageUpdateRequest(
        @NotNull ImageFilter filter
) {
}
