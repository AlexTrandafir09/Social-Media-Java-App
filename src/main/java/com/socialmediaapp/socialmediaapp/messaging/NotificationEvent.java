package com.socialmediaapp.socialmediaapp.messaging;

import com.socialmediaapp.socialmediaapp.notification.entity.NotificationType;

// Published to RabbitMQ when a like/comment/follow should notify someone.
// Duplicated (not shared) in notification-service, since the two services
// don't share a code module - only the wire shape (field names + enum
// constant names) needs to match.
public record NotificationEvent(
        Long recipientId,
        Long actorId,
        NotificationType type,
        Long referencePostId
) {
}
