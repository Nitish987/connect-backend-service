package com.conceptune.connect.database.models;

import com.conceptune.connect.database.orm.MapColumn;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class Story {

    @MapColumn(name = "id")
    private Long id;

    @MapColumn(name = "user_id")
    private String userId;

    @MapColumn(name = "type")
    private String type;

    @MapColumn(name = "content")
    private String content;

    @MapColumn(name = "created_at")
    private Timestamp createdAt;
}
