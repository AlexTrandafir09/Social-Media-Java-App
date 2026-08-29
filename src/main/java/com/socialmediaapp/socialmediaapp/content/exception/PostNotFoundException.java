package com.socialmediaapp.socialmediaapp.content;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(Long id) {
        super("Post not found: " + id);
    }
}
