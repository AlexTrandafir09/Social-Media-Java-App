package com.socialmediaapp.socialmediaapp.notification.service;

import com.socialmediaapp.socialmediaapp.notification.entity.NotificationType;
import com.socialmediaapp.socialmediaapp.messaging.NotificationEvent;
import com.socialmediaapp.socialmediaapp.messaging.NotificationEventPublisher;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.entity.UserPreference;
import com.socialmediaapp.socialmediaapp.user.service.UserPreferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private UserPreferenceService userPreferenceService;

    @Mock
    private NotificationEventPublisher notificationEventPublisher;

    private NotificationService notificationService;

    private User recipient;
    private User actor;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(userPreferenceService, notificationEventPublisher);
        recipient = User.builder().id(1L).username("alice").build();
        actor = User.builder().id(2L).username("bob").build();
    }

    @Test
    void notifyIfEnabled_publishesWhenPreferenceEnabled() {
        UserPreference preference = UserPreference.builder().notifyOnLike(true).build();
        when(userPreferenceService.getByUserId(1L)).thenReturn(preference);

        notificationService.notifyIfEnabled(recipient, actor, NotificationType.LIKE, 5L);

        verify(notificationEventPublisher).publish(new NotificationEvent(1L, 2L, NotificationType.LIKE, 5L));
    }

    @Test
    void notifyIfEnabled_skipsWhenPreferenceDisabled() {
        UserPreference preference = UserPreference.builder().notifyOnComment(false).build();
        when(userPreferenceService.getByUserId(1L)).thenReturn(preference);

        notificationService.notifyIfEnabled(recipient, actor, NotificationType.COMMENT, 5L);

        verify(notificationEventPublisher, never()).publish(any());
    }

    @Test
    void notifyIfEnabled_skipsWhenRecipientIsActor() {
        notificationService.notifyIfEnabled(recipient, recipient, NotificationType.FOLLOW, null);

        verify(notificationEventPublisher, never()).publish(any());
        verify(userPreferenceService, never()).getByUserId(any());
    }
}
