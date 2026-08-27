package com.socialmediaapp.socialmediaapp.user.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(max = 280) String bio,
        String avatarUrl
) {
}
