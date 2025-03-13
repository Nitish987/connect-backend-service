package com.conceptune.connect.database.models;

import com.conceptune.connect.database.orm.MapColumn;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class Group {

    @MapColumn(name = "id")
    private String id;

    @MapColumn(name = "name")
    private String name;

    @MapColumn(name = "photo")
    private String photo;

    @MapColumn(name = "description")
    private String description;

    @MapColumn(name = "member_count")
    private Integer memberCount;

    @MapColumn(name = "created_at")
    private Timestamp createdAt;

    @MapColumn(name = "updated_at")
    private Timestamp updatedAt;
}
