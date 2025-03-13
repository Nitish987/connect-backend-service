package com.conceptune.connect.database.repository;

import com.conceptune.connect.database.models.MessageToken;
import com.conceptune.connect.database.orm.AbstractRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MessageTokenRepository extends AbstractRepository<MessageToken> {

    public boolean save(MessageToken entity) throws DataAccessException {
        String sql = "INSERT INTO ct_message_token (user_id, token, created_at) VALUES (?, ?, ?)";
        int effect = jdbcTemplate.update(sql, entity.getUserId(), entity.getToken(), entity.getCreatedAt());
        return effect > 0;
    }

    public boolean updateToken(String userId, String token, Timestamp createdAt) throws DataAccessException {
        String sql = "UPDATE ct_message_token SET token = ?, created_at = ? WHERE user_id = ?";
        int effect = jdbcTemplate.update(sql, token, createdAt, userId);
        return effect > 0;
    }

    public String findTokenByUser(String userId) {
        String sql = "SELECT token FROM ct_message_token WHERE user_id = ? LIMIT 1";
        return jdbcTemplate.query(sql, rs -> {
            List<String> tokens = new ArrayList<>();
            while (rs.next()) {
                tokens.add(rs.getString("token"));
            }
            return tokens.isEmpty() ? null: tokens.get(0);
        }, userId);
    }

    /**
     * Returns message tokens of user those having signal pre-key shortage
     * @param threshold minimum number of pre-keys as threshold
     * @return List of Message Tokens
     * @throws DataAccessException if failed to get message tokens
     */
    public List<String> findAllTokensHavingSignalPreKeyShortage(int threshold) throws DataAccessException {
        String sql = "SELECT mt.token FROM ct_message_token mt JOIN ct_signal_device sd ON mt.user_id = sd.user_id WHERE sd.pre_key_count < ?";
        return jdbcTemplate.query(sql, rs -> {
            List<String> tokens = new ArrayList<>();
            while (rs.next()) {
                tokens.add(rs.getString("token"));
            }
            return tokens;
        }, threshold);
    }

    public boolean isExistByUser(String userId) throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM ct_message_token WHERE user_id = ?";
        return Integer.parseInt(jdbcTemplate.queryForMap(sql, userId).get("count").toString()) > 0;
    }
}
