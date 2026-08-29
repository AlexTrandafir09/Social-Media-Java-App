package com.socialmediaapp.notificationservice.notification.dto;

import com.socialmediaapp.notificationservice.notification.entity.NotificationType;
import jakarta.validation.constraints.NotNull;

public record NotificationCreateRequest(
        @NotNull Long recipientId,
        @NotNull NotificationType type,
        Long referencePostId
) {
}
