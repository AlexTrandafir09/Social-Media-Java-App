package com.socialmediaapp.socialmediaapp.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotNull Long authorId,
        @NotNull Long postId,
        @NotBlank @Size(max = 500) String content
) {
}
