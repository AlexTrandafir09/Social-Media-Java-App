package com.socialmediaapp.followservice.follow.exception;

public class DuplicateFollowException extends RuntimeException {

    public DuplicateFollowException(Long followerId, Long followingId) {
        super("User " + followerId + " already follows " + followingId);
    }
}
