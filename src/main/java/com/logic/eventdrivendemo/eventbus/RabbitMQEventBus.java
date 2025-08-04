package com.logic.eventdrivendemo.eventbus;

import com.logic.eventdrivendemo.model.CustomEvent;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "messaging.type", havingValue = "rabbitmq")
public class RabbitMQEventBus implements EventBus {
    private final AmqpTemplate amqpTemplate;
    @Value("${messaging.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${messaging.rabbitmq.routing-key}")
    private String routingKey;

    public RabbitMQEventBus(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void publish(CustomEvent event) {
        amqpTemplate.convertAndSend(exchangeName, routingKey, event);
    }
}
