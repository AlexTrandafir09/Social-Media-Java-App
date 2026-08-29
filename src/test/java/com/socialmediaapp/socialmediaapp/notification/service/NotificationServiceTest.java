package com.socialmediaapp.socialmediaapp.notification.service;

import com.socialmediaapp.socialmediaapp.notification.dto.NotificationCreateRequest;
import com.socialmediaapp.socialmediaapp.notification.entity.Notification;
import com.socialmediaapp.socialmediaapp.notification.entity.NotificationType;
import com.socialmediaapp.socialmediaapp.notification.exception.NotificationNotFoundException;
import com.socialmediaapp.socialmediaapp.notification.repository.NotificationRepository;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.entity.UserPreference;
import com.socialmediaapp.socialmediaapp.user.exception.UserNotFoundException;
import com.socialmediaapp.socialmediaapp.user.repository.UserRepository;
import com.socialmediaapp.socialmediaapp.user.service.UserPreferenceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Mock
    private UserPreferenceService userPreferenceService;

    private NotificationService notificationService;

    private User recipient;
    private User actor;
    private Notification notification;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, userRepository, userPreferenceService);
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
        authenticateAs(2L, "ROLE_USER");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Long userId, String... authorities) {
        var grantedAuthorities = List.of(authorities).stream().map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, grantedAuthorities));
    }

    @Test
    void createNotification_savesWhenValid() {
        NotificationCreateRequest request = new NotificationCreateRequest(1L, NotificationType.LIKE, 1L);
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
        NotificationCreateRequest request = new NotificationCreateRequest(1L, NotificationType.LIKE, 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.createNotification(request))
                .isInstanceOf(UserNotFoundException.class);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createNotification_throwsWhenActorNotFound() {
        NotificationCreateRequest request = new NotificationCreateRequest(1L, NotificationType.LIKE, 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(recipient));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.createNotification(request))
                .isInstanceOf(UserNotFoundException.class);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void getNotificationById_returnsNotificationWhenFound() {
        authenticateAs(1L, "ROLE_USER");
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
    void getNotificationById_throwsWhenNotRecipientAndNotAdmin() {
        authenticateAs(3L, "ROLE_USER");
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.getNotificationById(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getNotificationById_succeedsWhenAdminButNotRecipient() {
        authenticateAs(3L, "ROLE_ADMIN");
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        Notification result = notificationService.getNotificationById(1L);

        assertThat(result).isEqualTo(notification);
    }

    @Test
    void getNotificationsForUser_returnsList() {
        authenticateAs(1L, "ROLE_USER");
        when(notificationRepository.findByRecipientId(1L)).thenReturn(List.of(notification));

        List<Notification> result = notificationService.getNotificationsForUser(1L);

        assertThat(result).containsExactly(notification);
    }

    @Test
    void getNotificationsForUser_throwsWhenNotSelfAndNotAdmin() {
        authenticateAs(3L, "ROLE_USER");

        assertThatThrownBy(() -> notificationService.getNotificationsForUser(1L))
                .isInstanceOf(AccessDeniedException.class);

        verify(notificationRepository, never()).findByRecipientId(any());
    }

    @Test
    void markAsRead_setsReadTrue() {
        authenticateAs(1L, "ROLE_USER");
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
    void markAsRead_throwsWhenNotRecipientAndNotAdmin() {
        authenticateAs(3L, "ROLE_USER");
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(1L))
                .isInstanceOf(AccessDeniedException.class);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void deleteNotification_deletesWhenExists() {
        authenticateAs(1L, "ROLE_USER");
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(1L);

        verify(notificationRepository).deleteById(1L);
    }

    @Test
    void deleteNotification_throwsWhenNotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.deleteNotification(99L))
                .isInstanceOf(NotificationNotFoundException.class);

        verify(notificationRepository, never()).deleteById(any());
    }

    @Test
    void deleteNotification_throwsWhenNotRecipientAndNotAdmin() {
        authenticateAs(3L, "ROLE_USER");
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.deleteNotification(1L))
                .isInstanceOf(AccessDeniedException.class);

        verify(notificationRepository, never()).deleteById(any());
    }

    @Test
    void deleteNotification_succeedsWhenAdminButNotRecipient() {
        authenticateAs(3L, "ROLE_ADMIN");
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(1L);

        verify(notificationRepository).deleteById(1L);
    }

    @Test
    void notifyIfEnabled_savesWhenPreferenceEnabled() {
        UserPreference preference = UserPreference.builder().notifyOnLike(true).build();
        when(userPreferenceService.getByUserId(1L)).thenReturn(preference);

        notificationService.notifyIfEnabled(recipient, actor, NotificationType.LIKE, 5L);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void notifyIfEnabled_skipsWhenPreferenceDisabled() {
        UserPreference preference = UserPreference.builder().notifyOnComment(false).build();
        when(userPreferenceService.getByUserId(1L)).thenReturn(preference);

        notificationService.notifyIfEnabled(recipient, actor, NotificationType.COMMENT, 5L);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void notifyIfEnabled_skipsWhenRecipientIsActor() {
        notificationService.notifyIfEnabled(recipient, recipient, NotificationType.FOLLOW, null);

        verify(notificationRepository, never()).save(any());
        verify(userPreferenceService, never()).getByUserId(any());
    }
}
