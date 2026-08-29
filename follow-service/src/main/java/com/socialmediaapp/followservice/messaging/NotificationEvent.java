package com.socialmediaapp.followservice.messaging;

// Mirrors com.socialmediaapp.notificationservice.messaging.NotificationEvent on the
// consumer side. Duplicated deliberately - the services don't share a code module,
// only the wire shape (field names + enum constant names) has to match.
public record NotificationEvent(
        Long recipientId,
        Long actorId,
        NotificationType type,
        Long referencePostId
) {
}
