package com.conceptune.connect.constants;

import lombok.Getter;

@Getter
public enum MessageStatus {
    INITIATE("INITIATE"),
    PROGRESS("PROGRESS"),
    DELIVERED("DELIVERED"),
    READ("READ"),
    EDIT("EDIT"),
    DELETE("DELETE"),
    FAILED("FAILED");

    private final String value;

    MessageStatus(String value) {
        this.value = value;
    }
}
