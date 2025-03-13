package com.conceptune.connect.database.models;

import com.conceptune.connect.database.orm.MapColumn;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class GroupMember {

    @MapColumn(name = "id")
    private Long id;

    @MapColumn(name = "user_id")
    private String userId;

    @MapColumn(name = "group_id")
    private String groupId;

    @MapColumn(name = "role")
    private String role;

    @MapColumn(name = "joined_at")
    private Timestamp joinedAt;
}
