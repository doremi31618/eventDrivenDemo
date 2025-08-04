package com.logic.eventdrivendemo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@ConditionalOnProperty(name = "messaging.type", havingValue = "rabbitmq")
public class RabbitMQConfig {

    @Value("${messaging.rabbitmq.queue}")
    private String queueName;

    @Value("${messaging.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${messaging.rabbitmq.routing-key}")
    private String routingKey;

    @Bean
    public Queue eventQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public DirectExchange eventExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding binding(Queue eventQueue, DirectExchange eventExchange) {
        return BindingBuilder.bind(eventQueue).to(eventExchange).with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
