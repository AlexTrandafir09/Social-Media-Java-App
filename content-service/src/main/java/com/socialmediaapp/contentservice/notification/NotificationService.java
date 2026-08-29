package com.socialmediaapp.contentservice.notification;

import com.socialmediaapp.contentservice.messaging.NotificationEvent;
import com.socialmediaapp.contentservice.messaging.NotificationEventPublisher;
import com.socialmediaapp.contentservice.messaging.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Unlike the monolith's original version, this doesn't check the recipient's
// notification preferences before publishing - that lives in user-service's
// UserPreference table, and there's no cross-service call wired up for it yet.
// Same known simplification already made for follow-service's notifyOnFollow.
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationEventPublisher notificationEventPublisher;

    public void notifyIfEnabled(Long recipientId, Long actorId, NotificationType type, Long referencePostId) {
        if (recipientId.equals(actorId)) {
            return;
        }
        notificationEventPublisher.publish(new NotificationEvent(recipientId, actorId, type, referencePostId));
    }
}
