package com.socialmediaapp.contentservice.content.exception;

public class LikeNotFoundException extends RuntimeException {

    public LikeNotFoundException(Long userId, Long postId) {
        super("Like not found for user " + userId + " on post " + postId);
    }
}
