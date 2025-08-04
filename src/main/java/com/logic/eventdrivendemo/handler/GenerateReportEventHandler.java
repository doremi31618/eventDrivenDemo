package com.logic.eventdrivendemo.handler;

import com.logic.eventdrivendemo.model.CustomEvent;
import org.springframework.stereotype.Component;

@Component
public class GenerateReportEventHandler implements CustomEventHandler {
    @Override
    public String getType() {
        return "GenerateReport";
    }

    @Override
    public void handle(CustomEvent event) {
        System.out.println("Generating report with payload: " + event.getPayload());
    }
}
