package com.socialmediaapp.notificationservice.notification.service;

import com.socialmediaapp.notificationservice.messaging.NotificationEvent;
import com.socialmediaapp.notificationservice.notification.dto.NotificationCreateRequest;
import com.socialmediaapp.notificationservice.notification.entity.Notification;
import com.socialmediaapp.notificationservice.notification.exception.NotificationNotFoundException;
import com.socialmediaapp.notificationservice.notification.repository.NotificationRepository;
import com.socialmediaapp.notificationservice.security.SecurityUtils;
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

    public Notification createNotification(NotificationCreateRequest request) {
        Long actorId = SecurityUtils.getCurrentUserId();
        Notification notification = Notification.builder()
                .recipientId(request.recipientId())
                .actorId(actorId)
                .type(request.type())
                .referencePostId(request.referencePostId())
                .build();
        Notification saved = notificationRepository.save(notification);
        log.debug("Notification created: id={}, recipientId={}, type={}", saved.getId(), saved.getRecipientId(), request.type());
        return saved;
    }

    @Transactional(readOnly = true)
    public Notification getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        if (!SecurityUtils.isCurrentUserOrAdmin(notification.getRecipientId())) {
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

    public void createFromEvent(NotificationEvent event) {
        Notification notification = Notification.builder()
                .recipientId(event.recipientId())
                .actorId(event.actorId())
                .type(event.type())
                .referencePostId(event.referencePostId())
                .build();
        Notification saved = notificationRepository.save(notification);
        log.debug("Notification created from event: id={}, recipientId={}, type={}", saved.getId(), saved.getRecipientId(), event.type());
    }
}
