package com.socialmediaapp.socialmediaapp.notification;

import jakarta.validation.constraints.NotNull;

public record NotificationCreateRequest(
        @NotNull Long recipientId,
        @NotNull Long actorId,
        @NotNull NotificationType type,
        Long referencePostId
) {
}
