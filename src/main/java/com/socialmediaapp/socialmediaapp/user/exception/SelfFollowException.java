package com.socialmediaapp.socialmediaapp.user.exception;

public class SelfFollowException extends RuntimeException {

    public SelfFollowException() {
        super("A user cannot follow themselves");
    }
}
