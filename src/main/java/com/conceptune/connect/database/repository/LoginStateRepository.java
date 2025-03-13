package com.conceptune.connect.database.repository;

import com.conceptune.connect.database.models.LoginState;
import com.conceptune.connect.database.orm.AbstractRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class LoginStateRepository extends AbstractRepository<LoginState> {

    public boolean save(LoginState entity) throws DataAccessException {
        String sql = "INSERT INTO ct_login_state (id, user_id, token, created_at) VALUES (?, ?, ?, ?)";
        int effect = jdbcTemplate.update(sql, entity.getId(), entity.getUserId(), entity.getToken(), entity.getCreatedAt());
        return effect > 0;
    }

    public boolean updateToken(String id, String userId, String token, Timestamp createdAt) throws DataAccessException {
        String sql = "UPDATE ct_login_state SET token = ?, created_at = ? WHERE id = ? AND user_id = ?";
        int effect = jdbcTemplate.update(sql, token, createdAt, id, userId);
        return effect > 0;
    }

    public LoginState findById(String id) throws DataAccessException {
        String sql = "SELECT * FROM ct_login_state WHERE id = ? LIMIT 1";
        return jdbcTemplate.query(sql, rs -> {
            List<LoginState> state = map(rs, LoginState.class);
            return state.isEmpty() ? null : state.get(0);
        }, id);
    }

    public boolean deleteById(String id) throws DataAccessException {
        String sql = "DELETE FROM ct_login_state WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    public boolean deleteByUserId(String userId) throws DataAccessException {
        String sql = "DELETE FROM ct_login_state WHERE user_id = ?";
        return jdbcTemplate.update(sql, userId) > 0;
    }
}
