package com.socialmediaapp.socialmediaapp.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
        @NotBlank @Size(max = 2000) String content
) {
}
