package com.conceptune.connect.constants;

import lombok.Getter;

@Getter
public enum GroupRole {
    ADMIN("ADMIN"),
    MEMBER("MEMBER");

    private final String value;

    GroupRole(String value) {
        this.value = value;
    }
}
