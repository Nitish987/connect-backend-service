package com.conceptune.connect.database.repository;

import com.conceptune.connect.database.models.Group;
import com.conceptune.connect.database.orm.AbstractRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Log4j2
@Repository
public class GroupRepository extends AbstractRepository<Group> {

    public boolean save(Group group) {
        String sql = "INSERT INTO ct_groups (id, name, photo, description, member_count, created_at, updated_at) VALUES (?,?,?,?,?,?,?)";
        int effect = jdbcTemplate.update(sql, group.getId(), group.getName(), group.getPhoto(), group.getDescription(), group.getMemberCount(), group.getCreatedAt(), group.getUpdatedAt());
        return effect > 0;
    }

    public boolean incrementMemberCount(int inc) {
        String sql = "UPDATE ct_groups SET member_count = member_count + ? WHERE id = ?";
        return jdbcTemplate.update(sql, inc) > 0;
    }

    public boolean decrementMemberCount(int dec) {
        String sql = "UPDATE ct_groups SET member_count = member_count - ? WHERE id = ?";
        return jdbcTemplate.update(sql, dec) > 0;
    }

    public Group findById(String id) {
        String sql = "SELECT * FROM ct_groups WHERE id = ?";
        return jdbcTemplate.query(sql, rs -> {
            List<Group> groups = map(rs, Group.class);
            return groups.isEmpty() ? null : groups.get(0);
        }, id);
    }

    public List<Group> findAllByUser(String userId) {
        String sql = "SELECT g.* FROM ct_groups g JOIN ct_group_members gm ON g.id = gm.group_id WHERE gm.user_id = ?";
        return jdbcTemplate.query(sql, rs -> {
            return map(rs, Group.class);
        }, userId);
    }

    public boolean deleteById(String id) throws DataAccessException {
        String sql = "DELETE FROM ct_groups WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}
