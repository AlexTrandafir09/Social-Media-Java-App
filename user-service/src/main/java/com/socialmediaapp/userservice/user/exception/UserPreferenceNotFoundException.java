package com.socialmediaapp.userservice.user.exception;

public class UserPreferenceNotFoundException extends RuntimeException {

    public UserPreferenceNotFoundException(Long userId) {
        super("No preferences found for user: " + userId);
    }
}
