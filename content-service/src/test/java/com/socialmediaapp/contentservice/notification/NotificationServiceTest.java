package com.socialmediaapp.contentservice.notification;

import com.socialmediaapp.contentservice.messaging.NotificationEvent;
import com.socialmediaapp.contentservice.messaging.NotificationEventPublisher;
import com.socialmediaapp.contentservice.messaging.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationEventPublisher notificationEventPublisher;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationEventPublisher);
    }

    @Test
    void notifyIfEnabled_publishesWhenRecipientIsNotActor() {
        notificationService.notifyIfEnabled(1L, 2L, NotificationType.LIKE, 5L);

        verify(notificationEventPublisher).publish(new NotificationEvent(1L, 2L, NotificationType.LIKE, 5L));
    }

    @Test
    void notifyIfEnabled_skipsWhenRecipientIsActor() {
        notificationService.notifyIfEnabled(1L, 1L, NotificationType.FOLLOW, null);

        verify(notificationEventPublisher, never()).publish(any());
    }
}
