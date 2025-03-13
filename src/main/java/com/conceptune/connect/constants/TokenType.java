package com.conceptune.connect.constants;

import lombok.Getter;

@Getter
public enum TokenType {
    SIGN_IN("SIGN_IN"),
    NEW_USER("NEW_USER"),
    MFA("MFA"),
    FORGET_PIN("FORGET_PIN"),
    NEW_PIN("NEW_PIN"),
    IDENTITY("IDENTITY"),
    IDENTITY_VERIFIED("IDENTITY_VERIFIED"),
    PHONE_UPDATE("PHONE_UPDATE");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }
}
