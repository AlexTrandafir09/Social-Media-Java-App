package com.socialmediaapp.socialmediaapp.user.exception;

public class FollowNotFoundException extends RuntimeException {

    public FollowNotFoundException(Long followerId, Long followingId) {
        super("Follow relationship not found: " + followerId + " -> " + followingId);
    }
}
