package com.socialmediaapp.notificationservice.messaging;

import com.socialmediaapp.notificationservice.notification.entity.NotificationType;

public record NotificationEvent(
        Long recipientId,
        Long actorId,
        NotificationType type,
        Long referencePostId
) {
}
