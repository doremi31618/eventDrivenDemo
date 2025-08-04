package com.logic.eventdrivendemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data

public class CustomEvent implements Serializable {
    private String type;
    private String payload;

    public CustomEvent() {}

    public CustomEvent(String type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

}
