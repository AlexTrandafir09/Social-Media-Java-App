package com.socialmediaapp.followservice.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class RabbitMqConfig {

    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.created";

    public static final String ACTIVITY_EXCHANGE = "activity.exchange";
    public static final String ACTIVITY_ROUTING_KEY = "activity.recorded";

    @Bean
    @Profile("!test")
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    @Profile("!test")
    public TopicExchange activityExchange() {
        return new TopicExchange(ACTIVITY_EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
