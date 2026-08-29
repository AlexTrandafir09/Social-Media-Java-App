package com.socialmediaapp.socialmediaapp.user.controller;

import com.socialmediaapp.socialmediaapp.user.dto.FollowRequest;
import com.socialmediaapp.socialmediaapp.user.entity.Follow;
import com.socialmediaapp.socialmediaapp.user.service.FollowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping
    public ResponseEntity<Follow> follow(@Valid @RequestBody FollowRequest request) {
        Follow follow = followService.follow(request.followerId(), request.followingId());
        return ResponseEntity.status(HttpStatus.CREATED).body(follow);
    }

    @DeleteMapping
    public ResponseEntity<Void> unfollow(@RequestParam Long followerId, @RequestParam Long followingId) {
        followService.unfollow(followerId, followingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/following/{userId}")
    public List<Follow> getFollowing(@PathVariable Long userId) {
        return followService.getFollowing(userId);
    }

    @GetMapping("/followers/{userId}")
    public List<Follow> getFollowers(@PathVariable Long userId) {
        return followService.getFollowers(userId);
    }
}
