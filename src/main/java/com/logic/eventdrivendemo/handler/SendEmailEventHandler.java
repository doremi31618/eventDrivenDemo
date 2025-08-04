package com.logic.eventdrivendemo.handler;

import com.logic.eventdrivendemo.model.CustomEvent;
import org.springframework.stereotype.Component;

@Component
public class SendEmailEventHandler implements CustomEventHandler {
    @Override
    public String getType() {
        return "SendEmail";
    }

    @Override
    public void handle(CustomEvent event) {

        System.out.println("Sending email with payload: " + event.getPayload());
        // Simulate errors and store messages in the database
//        throw new RuntimeException("Simulated Email Sending Failure");
    }
}
