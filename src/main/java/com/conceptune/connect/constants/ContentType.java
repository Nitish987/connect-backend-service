package com.conceptune.connect.constants;

import lombok.Getter;

@Getter
public enum ContentType {
    NONE("NONE"),
    TEXT("TEXT"),
    PHOTO("PHOTO"),
    AUDIO("AUDIO"),
    VIDEO("VIDEO"),
    DOC("DOC"),
    KEY("KEY"),
    MESSAGE("MESSAGE"),
    INFO("INFO");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }
}
