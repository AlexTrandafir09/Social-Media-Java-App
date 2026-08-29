package com.socialmediaapp.socialmediaapp.notification.service;

import com.socialmediaapp.socialmediaapp.notification.dto.NotificationCreateRequest;
import com.socialmediaapp.socialmediaapp.notification.entity.Notification;
import com.socialmediaapp.socialmediaapp.notification.entity.NotificationType;
import com.socialmediaapp.socialmediaapp.notification.exception.NotificationNotFoundException;
import com.socialmediaapp.socialmediaapp.notification.repository.NotificationRepository;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.exception.UserNotFoundException;
import com.socialmediaapp.socialmediaapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    private NotificationService notificationService;

    private User recipient;
    private User actor;
    private Notification notification;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, userRepository);
        recipient = User.builder().id(1L).username("alice").build();
        actor = User.builder().id(2L).username("bob").build();
        notification = Notification.builder()
                .id(1L)
                .recipient(recipient)
                .actor(actor)
                .type(NotificationType.LIKE)
                .referencePostId(1L)
                .read(false)
                .build();
    }

    @Test
    void createNotification_savesWhenValid() {
        NotificationCreateRequest request = new NotificationCreateRequest(1L, 2L, NotificationType.LIKE, 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(recipient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(actor));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.createNotification(request);

        assertThat(result.getRecipient()).isEqualTo(recipient);
        assertThat(result.getActor()).isEqualTo(actor);
        assertThat(result.getType()).isEqualTo(NotificationType.LIKE);
    }

    @Test
    void createNotification_throwsWhenRecipientNotFound() {
        NotificationCreateRequest request = new NotificationCreateRequest(1L, 2L, NotificationType.LIKE, 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.createNotification(request))
                .isInstanceOf(UserNotFoundException.class);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createNotification_throwsWhenActorNotFound() {
        NotificationCreateRequest request = new NotificationCreateRequest(1L, 2L, NotificationType.LIKE, 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(recipient));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.createNotification(request))
                .isInstanceOf(UserNotFoundException.class);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void getNotificationById_returnsNotificationWhenFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        Notification result = notificationService.getNotificationById(1L);

        assertThat(result).isEqualTo(notification);
    }

    @Test
    void getNotificationById_throwsWhenNotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotificationById(99L))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    void getNotificationsForUser_returnsList() {
        when(notificationRepository.findByRecipientId(1L)).thenReturn(List.of(notification));

        List<Notification> result = notificationService.getNotificationsForUser(1L);

        assertThat(result).containsExactly(notification);
    }

    @Test
    void markAsRead_setsReadTrue() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification result = notificationService.markAsRead(1L);

        assertThat(result.isRead()).isTrue();
    }

    @Test
    void markAsRead_throwsWhenNotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(99L))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    void deleteNotification_deletesWhenExists() {
        when(notificationRepository.existsById(1L)).thenReturn(true);

        notificationService.deleteNotification(1L);

        verify(notificationRepository).deleteById(1L);
    }

    @Test
    void deleteNotification_throwsWhenNotFound() {
        when(notificationRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> notificationService.deleteNotification(99L))
                .isInstanceOf(NotificationNotFoundException.class);

        verify(notificationRepository, never()).deleteById(any());
    }
}
