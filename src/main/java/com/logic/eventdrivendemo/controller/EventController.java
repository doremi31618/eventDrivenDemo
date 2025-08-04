package com.logic.eventdrivendemo.controller;

import com.logic.eventdrivendemo.eventbus.EventBus;
import com.logic.eventdrivendemo.model.CustomEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController {

    private final EventBus eventBus;

    public EventController(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @PostMapping("/send")
    public String sendEvent(@RequestParam String type, @RequestParam String payload) {
        CustomEvent event = new CustomEvent(type, payload);
        eventBus.publish(event);
        return "Event sent: " + type;
    }
}