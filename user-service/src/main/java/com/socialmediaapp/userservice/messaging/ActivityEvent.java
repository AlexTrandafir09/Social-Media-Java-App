package com.socialmediaapp.userservice.messaging;

// Mirrors com.socialmediaapp.activitylogservice.messaging.ActivityEvent on the
// consumer side. Duplicated deliberately - the services don't share a code module,
// only the wire shape (field names + enum constant names) has to match.
public record ActivityEvent(
        Long actorId,
        ActivityAction action,
        String description
) {
}
