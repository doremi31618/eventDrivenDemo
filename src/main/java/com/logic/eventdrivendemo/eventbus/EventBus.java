package com.logic.eventdrivendemo.eventbus;

import com.logic.eventdrivendemo.model.CustomEvent;

public interface EventBus {
    void publish(CustomEvent event);
}
