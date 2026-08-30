package com.socialmediaapp.userservice.messaging;

public record ActivityEvent(
        Long actorId,
        ActivityAction action,
        String description
) {
}
