package com.socialmediaapp.contentservice.messaging;

public record ActivityEvent(
        Long actorId,
        ActivityAction action,
        String description
) {
}
