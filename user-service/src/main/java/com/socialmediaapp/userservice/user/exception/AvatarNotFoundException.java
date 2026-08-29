package com.socialmediaapp.userservice.user.exception;

public class AvatarNotFoundException extends RuntimeException {

    public AvatarNotFoundException(Long userId) {
        super("No avatar set for user: " + userId);
    }
}
