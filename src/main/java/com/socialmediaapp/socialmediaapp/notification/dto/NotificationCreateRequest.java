package com.socialmediaapp.socialmediaapp.notification.dto;

import com.socialmediaapp.socialmediaapp.notification.entity.NotificationType;
import jakarta.validation.constraints.NotNull;

public record NotificationCreateRequest(
        @NotNull Long recipientId,
        @NotNull Long actorId,
        @NotNull NotificationType type,
        Long referencePostId
) {
}
