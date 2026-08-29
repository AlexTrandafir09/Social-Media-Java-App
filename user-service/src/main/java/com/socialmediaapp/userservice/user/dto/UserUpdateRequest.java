package com.socialmediaapp.userservice.user.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(max = 280) String bio,
        String avatarUrl
) {
}
