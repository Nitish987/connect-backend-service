package com.conceptune.connect.constants;

import lombok.Getter;

@Getter
public enum ConnectType {
    SELF("SELF"),
    CONTACT("CONTACT"),
    GROUP("GROUP"),
    CLOUD("CLOUD"),
    SYSTEM("SYSTEM");

    private final String value;

    ConnectType(String value) {
        this.value = value;
    }
}
