package com.socialmediaapp.contentservice.notification;

import com.socialmediaapp.contentservice.messaging.NotificationEvent;
import com.socialmediaapp.contentservice.messaging.NotificationEventPublisher;
import com.socialmediaapp.contentservice.messaging.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
