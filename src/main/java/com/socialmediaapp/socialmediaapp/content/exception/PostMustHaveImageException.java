package com.socialmediaapp.socialmediaapp.content.exception;

public class PostMustHaveImageException extends RuntimeException {

    public PostMustHaveImageException() {
        super("A post must have at least one image");
    }
}
