package com.socialmediaapp.notificationservice.notification.repository;

import com.socialmediaapp.notificationservice.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientId(Long recipientId);
}
