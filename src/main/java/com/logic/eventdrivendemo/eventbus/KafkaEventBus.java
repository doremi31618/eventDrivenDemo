package com.logic.eventdrivendemo.eventbus;

import com.logic.eventdrivendemo.model.CustomEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "messaging.type", havingValue = "kafka")
public class KafkaEventBus implements EventBus {
    private final KafkaTemplate<String, CustomEvent> kafkaTemplate;
    @Value("${messaging.kafka.topic}")
    private String topicName;

    public KafkaEventBus(KafkaTemplate<String, CustomEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(CustomEvent event) {
        kafkaTemplate.send(topicName, event);
    }
}