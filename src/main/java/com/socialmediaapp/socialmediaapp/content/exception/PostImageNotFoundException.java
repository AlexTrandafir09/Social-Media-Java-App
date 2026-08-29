package com.socialmediaapp.socialmediaapp.content.exception;

public class PostImageNotFoundException extends RuntimeException {

    public PostImageNotFoundException(Long id) {
        super("Post image not found: " + id);
    }
}
