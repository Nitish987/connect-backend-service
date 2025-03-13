package com.conceptune.connect.database.models;

import com.conceptune.connect.database.orm.MapColumn;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class User {

    @MapColumn(name = "id")
    private String id;

    @MapColumn(name = "name")
    private String name;

    @MapColumn(name = "username")
    private String username;

    @MapColumn(name = "phone")
    private String phone;

    @MapColumn(name = "hash")
    private String hash;

    @MapColumn(name = "photo")
    private String photo;

    @MapColumn(name = "email")
    private String email;

    @MapColumn(name = "pin")
    private String pin;

    @MapColumn(name = "country")
    private String country;

    @MapColumn(name = "country_code")
    private String countryCode;

    @MapColumn(name = "title")
    private String title;

    @MapColumn(name = "is_active")
    private boolean isActive;

    @MapColumn(name = "mfa_status")
    private String mfaStatus;

    @MapColumn(name = "created_at")
    private Timestamp createdAt;

    @MapColumn(name = "updated_at")
    private Timestamp updatedAt;
}
