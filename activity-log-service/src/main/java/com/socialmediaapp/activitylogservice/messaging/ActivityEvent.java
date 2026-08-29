package com.socialmediaapp.activitylogservice.messaging;

import com.socialmediaapp.activitylogservice.activity.entity.ActivityAction;

// Mirrors com.socialmediaapp.socialmediaapp.messaging.ActivityEvent in the
// publisher. Duplicated deliberately - the two services don't share a code
// module, only the wire shape (field names + enum constant names) has to match.
public record ActivityEvent(
        Long actorId,
        ActivityAction action,
        String description
) {
}
