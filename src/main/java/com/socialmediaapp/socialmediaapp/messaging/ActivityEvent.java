package com.socialmediaapp.socialmediaapp.messaging;

import com.socialmediaapp.socialmediaapp.activity.ActivityAction;

public record ActivityEvent(Long actorId, ActivityAction action, String description) {
}
