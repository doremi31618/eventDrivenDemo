package com.logic.eventdrivendemo.listener;

import com.logic.eventdrivendemo.dispatcher.EventDispatcher;
import com.logic.eventdrivendemo.model.CustomEvent;
import com.logic.eventdrivendemo.model.EventRecord;
import com.logic.eventdrivendemo.processor.EventProcessor;
import com.logic.eventdrivendemo.repository.EventRecordRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "messaging.type", havingValue = "kafka")
public class KafkaEventListener {

    private final EventProcessor eventProcessor;

    public KafkaEventListener(EventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    @KafkaListener(topics = "${messaging.kafka.topic}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(CustomEvent event) {
        eventProcessor.processEvent(event);
    }
}