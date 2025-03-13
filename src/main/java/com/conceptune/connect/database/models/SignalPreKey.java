package com.conceptune.connect.database.models;

import com.conceptune.connect.database.orm.MapColumn;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class SignalPreKey {

    @MapColumn(name = "id")
    private Long id;

    @MapColumn(name = "user_id")
    private String userId;

    @MapColumn(name = "signal_device_id")
    private Long signalDeviceId;

    @MapColumn(name = "key_id")
    private Long keyId;

    @MapColumn(name = "pre_key")
    private String preKey;

    @MapColumn(name = "created_at")
    private Timestamp createdAt;
}
