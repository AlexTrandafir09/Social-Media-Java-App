package com.socialmediaapp.contentservice.content.controller;

import com.socialmediaapp.contentservice.content.dto.PostImageCreateRequest;
import com.socialmediaapp.contentservice.content.dto.PostImageUpdateRequest;
import com.socialmediaapp.contentservice.content.entity.PostImage;
import com.socialmediaapp.contentservice.content.service.PostImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/images")
@RequiredArgsConstructor
public class PostImageController {

    private final PostImageService postImageService;

    @PostMapping
    public ResponseEntity<PostImage> addImage(@PathVariable Long postId, @Valid @RequestBody PostImageCreateRequest request) {
        PostImage image = postImageService.addImage(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(image);
    }

    @GetMapping
    public List<PostImage> getImagesForPost(@PathVariable Long postId) {
        return postImageService.getImagesForPost(postId);
    }

    @PutMapping("/{id}")
    public PostImage updateFilter(@PathVariable Long postId, @PathVariable Long id, @Valid @RequestBody PostImageUpdateRequest request) {
        return postImageService.updateFilter(postId, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long postId, @PathVariable Long id) {
        postImageService.deleteImage(postId, id);
        return ResponseEntity.noContent().build();
    }
}
