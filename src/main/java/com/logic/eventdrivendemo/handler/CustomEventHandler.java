package com.logic.eventdrivendemo.handler;

import com.logic.eventdrivendemo.model.CustomEvent;

public interface CustomEventHandler {
    String getType();
    void handle(CustomEvent event);
}