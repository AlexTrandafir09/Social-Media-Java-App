package com.socialmediaapp.contentservice.content.controller;

import com.socialmediaapp.contentservice.content.dto.LikeRequest;
import com.socialmediaapp.contentservice.content.entity.Like;
import com.socialmediaapp.contentservice.content.service.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<Like> like(@Valid @RequestBody LikeRequest request) {
        Like like = likeService.like(request.postId());
        return ResponseEntity.status(HttpStatus.CREATED).body(like);
    }

    @DeleteMapping
    public ResponseEntity<Void> unlike(@RequestParam Long postId) {
        likeService.unlike(postId);
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
