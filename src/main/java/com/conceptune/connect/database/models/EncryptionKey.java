package com.conceptune.connect.database.models;

import com.conceptune.connect.database.orm.MapColumn;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class EncryptionKey {

    @MapColumn(name = "id")
    private Long id;

    @MapColumn(name = "user_id")
    private String userId;

    @MapColumn(name = "secret_key")
    private String secretKey;

    @MapColumn(name = "private_key")
    private String privateKey;

    @MapColumn(name = "public_key")
    private String publicKey;

    @MapColumn(name = "created_key")
    private Timestamp createdAt;
}
