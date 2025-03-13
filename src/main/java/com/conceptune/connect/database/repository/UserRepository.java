package com.conceptune.connect.database.repository;

import com.conceptune.connect.database.models.User;
import com.conceptune.connect.database.orm.AbstractRepository;
import com.google.cloud.storage.StorageException;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository
public class UserRepository extends AbstractRepository<User> {

    public boolean save(User user) throws DataAccessException {
        String sql = "INSERT INTO ct_user (id, name, username, phone, hash, photo, email, pin, country, country_code, title, is_active, mfa_status, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        int effect = jdbcTemplate.update(sql, user.getId(), user.getName(), user.getUsername(), user.getPhone(), user.getHash(), user.getPhoto(), user.getEmail(), user.getPin(), user.getCountry(), user.getCountryCode(), user.getTitle(), user.isActive(), user.getMfaStatus(), user.getCreatedAt(), user.getUpdatedAt());
        return effect > 0;
    }

    public boolean updatePassword(String userId, String password, Timestamp updatedAt) throws DataAccessException {
        String sql = "UPDATE ct_user SET password = ?, updated_at = ? WHERE id = ?";
        int effect = jdbcTemplate.update(sql, password, updatedAt, userId);
        return effect > 0;
    }

    public boolean updateName(String userId, String name, Timestamp updatedAt) throws DataAccessException {
        String sql = "UPDATE ct_user SET name = ?, updated_at = ? WHERE id = ?";
        int effect = jdbcTemplate.update(sql, name, updatedAt, userId);
        return effect > 0;
    }

    public boolean updateEmail(String userId, String email, Timestamp updatedAt) throws DataAccessException {
        String sql = "UPDATE ct_user SET email = ?, updated_at = ? WHERE id = ?";
        int effect = jdbcTemplate.update(sql, email, updatedAt, userId);
        return effect > 0;
    }

    public boolean updateMultiFactorAuthStatus(String userId, String mfaStatus, Timestamp updatedAt) throws DataAccessException {
        String sql = "UPDATE ct_user SET mfa_status = ?, updated_at = ? WHERE id = ?";
        int effect = jdbcTemplate.update(sql, mfaStatus, updatedAt, userId);
        return effect > 0;
    }

    public boolean updateActiveStatus(String userId, Boolean activeStatus, Timestamp updatedAt) throws DataAccessException {
        String sql = "UPDATE ct_user SET is_active = ?, updated_at = ? WHERE id = ?";
        int effect = jdbcTemplate.update(sql, activeStatus, updatedAt, userId);
        return effect > 0;
    }

    public boolean updatePhoto(String userId, String filename, byte[] photoBytes, Timestamp updatedAt) throws DataAccessException, StorageException {
        String downloadUrl = storageTemplate.upload("photos", filename, "image/png", photoBytes);
        String sql = "UPDATE ct_user SET photo = ?, updated_at = ? WHERE id = ?";
        int effect = jdbcTemplate.update(sql, downloadUrl, updatedAt, userId);
        return effect > 0;
    }

    public User findById(String id) throws DataAccessException {
        String sql = "SELECT * FROM ct_user WHERE id = ? LIMIT 1";
        return jdbcTemplate.query(sql, rs -> {
            List<User> users = map(rs, User.class);
            return users.isEmpty() ? null : users.get(0);
        }, id);
    }

    public User findByPhone(String phone) throws DataAccessException {
        String sql = "SELECT * FROM ct_user WHERE phone = ? LIMIT 1";
        return jdbcTemplate.query(sql, rs -> {
            List<User> users = map(rs, User.class);
            return users.isEmpty() ? null : users.get(0);
        }, phone);
    }

    public User findByHash(String hash) throws DataAccessException {
        String sql = "SELECT * FROM ct_user WHERE hash = ? LIMIT 1";
        return jdbcTemplate.query(sql, rs -> {
            List<User> users = map(rs, User.class);
            return users.isEmpty() ? null : users.get(0);
        }, hash);
    }

    public User findByUsername(String username) throws DataAccessException {
        String sql = "SELECT * FROM ct_user WHERE username = ? LIMIT 1";
        return jdbcTemplate.query(sql, rs -> {
            List<User> users = map(rs, User.class);
            return users.isEmpty() ? null : users.get(0);
        }, username);
    }

    public List<User> findAllByPhones(List<String> phones) throws DataAccessException {
        String sql = "SELECT * FROM ct_user WHERE phone IN (:phones) OR CONCAT(country_code, phone) IN (:phones)";
        return npJdbcTemplate.query(sql, Map.of("phones", phones), rs -> {
            return map(rs, User.class);
        });
    }

    public List<User> findAllByHashes(List<String> hashes) throws DataAccessException {
        String sql = "SELECT * FROM ct_user WHERE hash IN (:hashes)";
        return npJdbcTemplate.query(sql, Map.of("hashes", hashes), rs -> {
            return map(rs, User.class);
        });
    }

    public List<User> findAllByIds(List<String> ids) throws DataAccessException {
        String sql = "SELECT * FROM ct_user WHERE id IN (:ids)";
        return npJdbcTemplate.query(sql, Map.of("ids", ids), rs -> {
            return map(rs, User.class);
        });
    }

    /**
     * Returns users in a group
     * @param groupId Group Id
     * @return List of users
     * @throws DataAccessException if an error occurs
     */
    public List<User> findAllInGroup(String groupId) throws DataAccessException {
        String sql = "SELECT u.* FROM ct_user u JOIN ct_group_members gm ON u.id = gm.user_id AND gm.group_id = ? ORDER BY gm.role ASC";
        return jdbcTemplate.query(sql, rs -> {
            return map(rs, User.class);
        }, groupId);
    }

    public boolean deleteById(String id) throws DataAccessException {
        String sql = "DELETE FROM ct_user WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    public boolean isExistByPhone(String phone) throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM ct_user WHERE phone = ?";
        return Integer.parseInt(jdbcTemplate.queryForMap(sql, phone).get("count").toString()) > 0;
    }
}
