package com.socialmediaapp.socialmediaapp.content.controller;

import com.socialmediaapp.socialmediaapp.content.dto.PostImageCreateRequest;
import com.socialmediaapp.socialmediaapp.content.dto.PostImageUpdateRequest;
import com.socialmediaapp.socialmediaapp.content.entity.PostImage;
import com.socialmediaapp.socialmediaapp.content.service.PostImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
