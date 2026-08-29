package com.socialmediaapp.socialmediaapp.content.controller;

import com.socialmediaapp.socialmediaapp.content.dto.LikeRequest;
import com.socialmediaapp.socialmediaapp.content.entity.Like;
import com.socialmediaapp.socialmediaapp.content.service.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<Like> like(@Valid @RequestBody LikeRequest request) {
        Like like = likeService.like(request.userId(), request.postId());
        return ResponseEntity.status(HttpStatus.CREATED).body(like);
    }

    @DeleteMapping
    public ResponseEntity<Void> unlike(@RequestParam Long userId, @RequestParam Long postId) {
        likeService.unlike(userId, postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/post/{postId}")
    public List<Like> getLikesForPost(@PathVariable Long postId) {
        return likeService.getLikesForPost(postId);
    }

    @GetMapping("/post/{postId}/count")
    public long countLikesForPost(@PathVariable Long postId) {
        return likeService.countLikesForPost(postId);
    }
}
