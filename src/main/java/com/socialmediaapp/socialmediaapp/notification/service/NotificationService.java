package com.socialmediaapp.socialmediaapp.notification.service;

import com.socialmediaapp.socialmediaapp.notification.dto.NotificationCreateRequest;
import com.socialmediaapp.socialmediaapp.notification.entity.Notification;
import com.socialmediaapp.socialmediaapp.notification.entity.NotificationType;
import com.socialmediaapp.socialmediaapp.notification.exception.NotificationNotFoundException;
import com.socialmediaapp.socialmediaapp.notification.repository.NotificationRepository;
import com.socialmediaapp.socialmediaapp.security.SecurityUtils;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.entity.UserPreference;
import com.socialmediaapp.socialmediaapp.user.exception.UserNotFoundException;
import com.socialmediaapp.socialmediaapp.user.repository.UserRepository;
import com.socialmediaapp.socialmediaapp.user.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserPreferenceService userPreferenceService;

    public Notification createNotification(NotificationCreateRequest request) {
        Long actorId = SecurityUtils.getCurrentUserId();
        User recipient = userRepository.findById(request.recipientId())
                .orElseThrow(() -> new UserNotFoundException(request.recipientId()));
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new UserNotFoundException(actorId));
        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(request.type())
                .referencePostId(request.referencePostId())
                .build();
        Notification saved = notificationRepository.save(notification);
        log.debug("Notification created: id={}, recipientId={}, type={}", saved.getId(), recipient.getId(), request.type());
        return saved;
    }

    @Transactional(readOnly = true)
    public Notification getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        if (!SecurityUtils.isCurrentUserOrAdmin(notification.getRecipient().getId())) {
            throw new AccessDeniedException("You can only view your own notifications");
        }
        return notification;
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(Long recipientId) {
        if (!SecurityUtils.isCurrentUserOrAdmin(recipientId)) {
            throw new AccessDeniedException("You can only view your own notifications");
        }
        return notificationRepository.findByRecipientId(recipientId);
    }

    public Notification markAsRead(Long id) {
        Notification notification = getNotificationById(id);
        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        log.debug("Notification marked read: id={}", id);
        return saved;
    }

    public void deleteNotification(Long id) {
        getNotificationById(id);
        notificationRepository.deleteById(id);
        log.debug("Notification deleted: id={}", id);
    }

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
        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .referencePostId(referencePostId)
                .build();
        notificationRepository.save(notification);
        log.debug("Notification created: recipientId={}, actorId={}, type={}", recipient.getId(), actor.getId(), type);
    }
}
