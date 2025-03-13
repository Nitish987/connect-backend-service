package com.conceptune.connect.database.models;

import com.conceptune.connect.database.orm.MapColumn;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class SignalDevice {

    @MapColumn(name = "id")
    private Long id;

    @MapColumn(name = "user_id")
    private String userId;

    @MapColumn(name = "registration_id")
    private Long registrationId;

    @MapColumn(name = "signed_pre_key")
    private String signedPreKey;

    @MapColumn(name = "signature")
    private String signature;

    @MapColumn(name = "identity_key")
    private String identityKey;

    @MapColumn(name = "pre_key_count")
    private Integer preKeyCount;

    @MapColumn(name = "refresh_at")
    private Timestamp refreshAt;

    @MapColumn(name = "updated_at")
    private Timestamp updatedAt;

    @MapColumn(name = "created_at")
    private Timestamp createdAt;
}
