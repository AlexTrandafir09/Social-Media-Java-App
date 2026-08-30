package com.socialmediaapp.contentservice.content.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostUpdateRequest(
        @NotBlank @Size(max = 2000) String content,
        List<Long> keepImageIds,
        @Valid List<PostImageInput> newImages
) {
}
