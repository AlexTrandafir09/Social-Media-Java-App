package com.socialmediaapp.activitylogservice.messaging;

import com.socialmediaapp.activitylogservice.activity.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityEventListener {

    private final ActivityLogService activityLogService;

    @RabbitListener(queues = RabbitMqConfig.ACTIVITY_QUEUE)
    public void onActivityEvent(ActivityEvent event) {
        log.debug("Received activity event: actorId={}, action={}", event.actorId(), event.action());
        activityLogService.recordFromEvent(event);
    }
}
