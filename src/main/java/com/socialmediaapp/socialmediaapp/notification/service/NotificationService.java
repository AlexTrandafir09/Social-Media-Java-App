package com.socialmediaapp.socialmediaapp.notification.service;

import com.socialmediaapp.socialmediaapp.notification.entity.NotificationType;
import com.socialmediaapp.socialmediaapp.messaging.NotificationEvent;
import com.socialmediaapp.socialmediaapp.messaging.NotificationEventPublisher;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.entity.UserPreference;
import com.socialmediaapp.socialmediaapp.user.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserPreferenceService userPreferenceService;
    private final NotificationEventPublisher notificationEventPublisher;

    public void notifyIfEnabled(User recipient, User actor, NotificationType type, Long referencePostId) {
        if (recipient.getId().equals(actor.getId())) {
            return;
        }
        UserPreference preference = userPreferenceService.getByUserId(recipient.getId());
        boolean enabled = switch (type) {
            case LIKE -> preference.isNotifyOnLike();
            case COMMENT -> preference.isNotifyOnComment();
            case FOLLOW -> preference.isNotifyOnFollow();
        };
        if (!enabled) {
            return;
        }
        notificationEventPublisher.publish(new NotificationEvent(recipient.getId(), actor.getId(), type, referencePostId));
    }
}
