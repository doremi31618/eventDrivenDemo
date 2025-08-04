package com.logic.eventdrivendemo.dispatcher;

import com.logic.eventdrivendemo.handler.CustomEventHandler;
import com.logic.eventdrivendemo.model.CustomEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EventDispatcher {
    private final Map<String, CustomEventHandler> handlerMap = new HashMap<>();

    @Autowired
    public EventDispatcher(List<CustomEventHandler> handlers) {
        for (CustomEventHandler handler : handlers) {
            handlerMap.put(handler.getType(), handler);
        }
    }

    public void dispatch(CustomEvent event) {
        CustomEventHandler handler = handlerMap.get(event.getType());
        if (handler != null) {
            handler.handle(event);
        } else {
            System.out.println("No handler found for event type: " + event.getType());
        }
    }
}