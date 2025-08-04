package com.logic.eventdrivendemo.listener;

import ch.qos.logback.classic.Logger;
import com.logic.eventdrivendemo.dispatcher.EventDispatcher;
import com.logic.eventdrivendemo.model.CustomEvent;
import com.logic.eventdrivendemo.model.EventRecord;
import com.logic.eventdrivendemo.processor.EventProcessor;
import com.logic.eventdrivendemo.repository.EventRecordRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "messaging.type", havingValue = "rabbitmq")
public class RabbitMQEventListener {

    private final EventProcessor eventProcessor;

    public RabbitMQEventListener(EventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    @RabbitListener(queues = "${messaging.rabbitmq.queue}")
    public void listen(CustomEvent event) {
        System.out.println("Event received!");
        eventProcessor.processEvent(event);
    }
}
