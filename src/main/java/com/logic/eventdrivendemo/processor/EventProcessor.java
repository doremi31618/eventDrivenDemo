package com.logic.eventdrivendemo.processor;

import com.logic.eventdrivendemo.dispatcher.EventDispatcher;
import com.logic.eventdrivendemo.model.CustomEvent;
import com.logic.eventdrivendemo.model.EventRecord;
import com.logic.eventdrivendemo.repository.EventRecordRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EventProcessor {

    private final EventDispatcher dispatcher;
    private final EventRecordRepository eventRecordRepository;

    public EventProcessor(EventDispatcher dispatcher, EventRecordRepository eventRecordRepository) {
        this.dispatcher = dispatcher;
        this.eventRecordRepository = eventRecordRepository;
    }

    public void processEvent(CustomEvent event) {
        EventRecord record = new EventRecord();
        record.setType(event.getType());
        record.setPayload(event.getPayload());
        record.setProcessedAt(LocalDateTime.now());
        try {
            dispatcher.dispatch(event);
            record.setStatus("SUCCESS");
        } catch (Exception e) {
            record.setStatus("FAILURE");
            record.setErrorMessage(e.getMessage());
        }
        eventRecordRepository.save(record);
    }
}
