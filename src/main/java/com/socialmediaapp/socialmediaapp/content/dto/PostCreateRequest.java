package com.socialmediaapp.socialmediaapp.content.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostCreateRequest(
        @NotBlank @Size(max = 2000) String content,
        @NotEmpty @Valid List<PostImageInput> images
) {
}
