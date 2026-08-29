package com.socialmediaapp.userservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(ActivityEvent event) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.ACTIVITY_EXCHANGE, RabbitMqConfig.ACTIVITY_ROUTING_KEY, event);
        log.debug("Published activity event: actorId={}, action={}", event.actorId(), event.action());
    }
}
