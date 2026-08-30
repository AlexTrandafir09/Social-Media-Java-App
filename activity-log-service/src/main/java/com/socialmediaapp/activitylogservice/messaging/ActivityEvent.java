package com.socialmediaapp.activitylogservice.messaging;

import com.socialmediaapp.activitylogservice.activity.entity.ActivityAction;

public record ActivityEvent(
        Long actorId,
        ActivityAction action,
        String description
) {
}
