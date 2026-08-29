package com.socialmediaapp.socialmediaapp.notification;

import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.exception.UserNotFoundException;
import com.socialmediaapp.socialmediaapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public Notification createNotification(NotificationCreateRequest request) {
        User recipient = userRepository.findById(request.recipientId())
                .orElseThrow(() -> new UserNotFoundException(request.recipientId()));
        User actor = userRepository.findById(request.actorId())
                .orElseThrow(() -> new UserNotFoundException(request.actorId()));
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
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(Long recipientId) {
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
        if (!notificationRepository.existsById(id)) {
            throw new NotificationNotFoundException(id);
        }
        notificationRepository.deleteById(id);
        log.debug("Notification deleted: id={}", id);
    }
}
