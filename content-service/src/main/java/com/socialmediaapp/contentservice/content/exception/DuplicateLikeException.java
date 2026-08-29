package com.socialmediaapp.contentservice.content.exception;

public class DuplicateLikeException extends RuntimeException {

    public DuplicateLikeException(Long userId, Long postId) {
        super("User " + userId + " already liked post " + postId);
    }
}
