package com.conceptune.connect.constants;

import lombok.Getter;

@Getter
public enum FcmQuery {
    PRE_KEY_FILLING("PRE_KEY_FILLING"),
    MESSAGE("MESSAGE");

    private final String value;

    FcmQuery(String value) {
        this.value = value;
    }
}
