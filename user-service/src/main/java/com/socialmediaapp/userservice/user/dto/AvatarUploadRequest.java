package com.socialmediaapp.userservice.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AvatarUploadRequest(
        @NotBlank @Pattern(regexp = "^image/.+") String contentType,
        @NotBlank String data
) {
}
