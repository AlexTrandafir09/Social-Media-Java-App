package com.socialmediaapp.contentservice.content.exception;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(Long id) {
        super("Post not found: " + id);
    }
}
