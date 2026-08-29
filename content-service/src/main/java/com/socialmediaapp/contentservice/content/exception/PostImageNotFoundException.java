package com.socialmediaapp.contentservice.content.exception;

public class PostImageNotFoundException extends RuntimeException {

    public PostImageNotFoundException(Long id) {
        super("Post image not found: " + id);
    }
}
