package com.socialmediaapp.notificationservice.messaging;

import com.socialmediaapp.notificationservice.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMqConfig.NOTIFICATION_QUEUE)
    public void onNotificationEvent(NotificationEvent event) {
        log.debug("Received notification event: recipientId={}, actorId={}, type={}",
                event.recipientId(), event.actorId(), event.type());
        notificationService.createFromEvent(event);
    }
}
