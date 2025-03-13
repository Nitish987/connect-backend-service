package com.conceptune.connect.database.repository;

import com.conceptune.connect.database.models.GroupMember;
import com.conceptune.connect.database.orm.AbstractRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.SQLException;
import java.util.*;

@Repository
public class GroupMemberRepository extends AbstractRepository<GroupMember> {

    public List<String> findMemberIdsFromCache(String groupId) {
        String sql = "SELECT members FROM ct_group_members_cache WHERE group_id = ? LIMIT 1";
        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                String[] memberArray = (String[]) rs.getArray("members").getArray();
                return Arrays.asList(memberArray);
            }

            return new ArrayList<>();
        }, groupId);
    }

    public boolean saveMemberIdsInCache(String groupId, List<String> memberIds) throws SQLException {
        Array memberArray = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection().createArrayOf("text", memberIds.toArray());
        String sql = "INSERT INTO ct_group_members_cache (group_id, members) VALUES (?,?)";
        return jdbcTemplate.update(sql, groupId, memberArray) > 0;
    }

    public boolean appendMemberIdInCache(String groupId, String memberId) throws SQLException {
        String sql = "UPDATE ct_group_members_cache SET members = array_append(members, ?) WHERE group_id = ?";
        return jdbcTemplate.update(sql, memberId, groupId) > 0;
    }

    public boolean appendMemberIdsInCache(String groupId, List<String> memberIds) throws SQLException {
        Array memberArray = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection().createArrayOf("text", memberIds.toArray());
        String sql = "UPDATE ct_group_members_cache SET members = array_cat(members, ?) WHERE group_id = ?";
        return jdbcTemplate.update(sql, memberArray, groupId) > 0;
    }

    public boolean updateMemberIdsInCache(String groupId, List<String> memberIds) throws SQLException {
        Array memberArray = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection().createArrayOf("text", memberIds.toArray());
        String sql = "UPDATE ct_group_members_cache SET members = ? WHERE group_id = ?";
        return jdbcTemplate.update(sql, groupId, memberArray) > 0;
    }

    public boolean deleteMemberIdsInCache(String groupId, List<String> membersIds) {
        StringBuilder sql = new StringBuilder("UPDATE ct_group_members_cache SET members = ");

        for (int i = 0; i < membersIds.size(); i++) {
            if (i == 0) {
                sql.append("array_remove(members, ?)");
            } else {
                sql.append(" = array_remove(members, ?)");
            }
        }
        sql.append(" WHERE group_id = ?");

        Object[] params = new Object[membersIds.size() + 1];
        System.arraycopy(membersIds.toArray(), 0, params, 0, membersIds.size());
        params[membersIds.size()] = groupId;

        return  jdbcTemplate.update(sql.toString(), params) > 0;
    }

    public boolean deleteMemberCache(String groupId) {
        String sql = "DELETE FROM ct_group_members_cache WHERE group_id =?";
        return jdbcTemplate.update(sql, groupId) > 0;
    }

    public boolean save(GroupMember member) throws DataAccessException {
        String sql = "INSERT INTO ct_group_members (user_id, group_id, role, joined_at) VALUES (?,?,?,?)";
        int effect = jdbcTemplate.update(sql, member.getUserId(), member.getGroupId(), member.getRole(), member.getJoinedAt());
        return effect > 0;
    }

    public boolean batchSave(List<GroupMember> members) throws DataAccessException {
        String sql = "INSERT INTO ct_group_members (user_id, group_id, role, joined_at) VALUES (?,?,?,?)";
        int[][] effect = jdbcTemplate.batchUpdate(sql, members, 10, (ps, member) -> {
            ps.setString(1, member.getUserId());
            ps.setString(2, member.getGroupId());
            ps.setString(3, member.getRole());
            ps.setTimestamp(4, member.getJoinedAt());
        });
        return effect.length > 0;
    }

    public boolean updateRoleByGroupAndUser(String groupId, String userId, String role) {
        String sql = "UPDATE ct_group_members SET role = ? WHERE group_id = ? AND user_id = ?";
        int effect = jdbcTemplate.update(sql, role, groupId, userId);
        return effect > 0;
    }

    public GroupMember findByGroupAndUser(String groupId, String userId) throws DataAccessException {
        String sql = "SELECT * FROM ct_group_members WHERE group_id = ? AND user_id = ? LIMIT 1";
        return jdbcTemplate.query(sql, rs -> {
            List<GroupMember> members = map(rs, GroupMember.class);
            return members.isEmpty() ? null : members.get(0);
        }, groupId, userId);
    }

    public List<GroupMember> findAllByGroup(String groupId) throws DataAccessException {
        String sql = "SELECT * FROM ct_group_members WHERE group_id = ?";
        return jdbcTemplate.query(sql, rs -> {
            return map(rs, GroupMember.class);
        }, groupId);
    }

    public boolean deleteByGroupAndUsers(String groupId, List<String> userIds) throws DataAccessException {
        String sql = "DELETE FROM ct_group_members WHERE group_id = :groupId AND user_id IN (:userIds)";
        return npJdbcTemplate.update(sql, Map.of("groupId", groupId, "userIds", userIds)) > 0;
    }

    public int countByGroup(String groupId) throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM ct_group_members WHERE group_id = ?";
        return Integer.parseInt(jdbcTemplate.queryForMap(sql, groupId).get("count").toString());
    }
}
