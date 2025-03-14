package com.conceptune.connect.constants;

import lombok.Getter;

@Getter
public enum Gender {
    MALE("M"),
    FEMALE("F"),
    OTHER("O"),
    UNKNOWN("U");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

}
