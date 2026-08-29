package com.socialmediaapp.followservice.follow.exception;

public class SelfFollowException extends RuntimeException {

    public SelfFollowException() {
        super("A user cannot follow themselves");
    }
}
