package com.socialmediaapp.followservice.follow.controller;

import com.socialmediaapp.followservice.follow.dto.FollowRequest;
import com.socialmediaapp.followservice.follow.entity.Follow;
import com.socialmediaapp.followservice.follow.service.FollowService;
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
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping
    public ResponseEntity<Follow> follow(@Valid @RequestBody FollowRequest request) {
        Follow follow = followService.follow(request.followingId());
        return ResponseEntity.status(HttpStatus.CREATED).body(follow);
    }

    @DeleteMapping
    public ResponseEntity<Void> unfollow(@RequestParam Long followingId) {
        followService.unfollow(followingId);
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
