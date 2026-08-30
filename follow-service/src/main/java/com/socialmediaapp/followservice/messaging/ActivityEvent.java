package com.socialmediaapp.followservice.messaging;

public record ActivityEvent(
        Long actorId,
        ActivityAction action,
        String description
) {
}
