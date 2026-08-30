package com.socialmediaapp.followservice.messaging;

public record NotificationEvent(
        Long recipientId,
        Long actorId,
        NotificationType type,
        Long referencePostId
) {
}
