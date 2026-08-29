package com.socialmediaapp.activitylogservice.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class RabbitMqConfig {

    public static final String ACTIVITY_EXCHANGE = "activity.exchange";
    public static final String ACTIVITY_QUEUE = "activity.events.queue";
    public static final String ACTIVITY_ROUTING_KEY = "activity.recorded";

    @Bean
    @Profile("!test")
    public TopicExchange activityExchange() {
        return new TopicExchange(ACTIVITY_EXCHANGE, true, false);
    }

    @Bean
    @Profile("!test")
    public Queue activityQueue() {
        return new Queue(ACTIVITY_QUEUE, true);
    }

    @Bean
    @Profile("!test")
    public Binding activityBinding(Queue activityQueue, TopicExchange activityExchange) {
        return BindingBuilder.bind(activityQueue).to(activityExchange).with(ACTIVITY_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
