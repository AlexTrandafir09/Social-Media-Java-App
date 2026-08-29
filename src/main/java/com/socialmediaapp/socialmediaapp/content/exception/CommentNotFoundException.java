package com.socialmediaapp.socialmediaapp.content;

public class CommentNotFoundException extends RuntimeException {

    public CommentNotFoundException(Long id) {
        super("Comment not found: " + id);
    }
}
