package com.conceptune.connect.constants;

import lombok.Getter;

@Getter
public enum MultiFactorAuthStatus {
    DISABLED("DISABLED"),
    USER_PIN("USER_PIN");

    private final String value;

    MultiFactorAuthStatus(String value) {
        this.value = value;
    }

}
