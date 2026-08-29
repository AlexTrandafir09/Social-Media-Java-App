package com.socialmediaapp.contentservice.content.dto;

import com.socialmediaapp.contentservice.content.entity.ImageFilter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PostImageCreateRequest(
        @NotBlank String storageKey,
        @NotBlank @Pattern(regexp = "^image/.+") String contentType,
        @NotBlank String data,
        ImageFilter filter
) {
}
