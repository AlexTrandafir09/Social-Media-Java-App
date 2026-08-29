package com.socialmediaapp.socialmediaapp.content;

public class PostMustHaveImageException extends RuntimeException {

    public PostMustHaveImageException() {
        super("A post must have at least one image");
    }
}
