package com.socialmediaapp.socialmediaapp.content;

import jakarta.validation.constraints.NotNull;

public record PostImageUpdateRequest(
        @NotNull ImageFilter filter
) {
}
