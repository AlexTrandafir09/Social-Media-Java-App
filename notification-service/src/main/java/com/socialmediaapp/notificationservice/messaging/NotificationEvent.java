package com.socialmediaapp.notificationservice.messaging;

import com.socialmediaapp.notificationservice.notification.entity.NotificationType;

// Mirrors com.socialmediaapp.socialmediaapp.messaging.NotificationEvent in the
// publisher. Duplicated deliberately - the two services don't share a code
// module, only the wire shape (field names + enum constant names) has to match.
public record NotificationEvent(
        Long recipientId,
        Long actorId,
        NotificationType type,
        Long referencePostId
) {
}
