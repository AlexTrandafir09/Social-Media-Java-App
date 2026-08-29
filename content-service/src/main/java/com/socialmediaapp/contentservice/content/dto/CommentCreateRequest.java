package com.socialmediaapp.contentservice.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotNull Long postId,
        @NotBlank @Size(max = 500) String content
) {
}
