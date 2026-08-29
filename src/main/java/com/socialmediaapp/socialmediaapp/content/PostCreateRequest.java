package com.socialmediaapp.socialmediaapp.content;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostCreateRequest(
        @NotNull Long authorId,
        @NotBlank @Size(max = 2000) String content,
        @NotEmpty @Valid List<PostImageInput> images
) {
}
