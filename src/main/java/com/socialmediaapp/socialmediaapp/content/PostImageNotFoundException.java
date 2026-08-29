package com.socialmediaapp.socialmediaapp.content;

public class PostImageNotFoundException extends RuntimeException {

    public PostImageNotFoundException(Long id) {
        super("Post image not found: " + id);
    }
}
