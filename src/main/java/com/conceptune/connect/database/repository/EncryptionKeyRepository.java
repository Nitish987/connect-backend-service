package com.conceptune.connect.database.repository;

import com.conceptune.connect.database.models.EncryptionKey;
import com.conceptune.connect.database.orm.AbstractRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EncryptionKeyRepository extends AbstractRepository<EncryptionKey> {

    public boolean save(EncryptionKey entity) throws DataAccessException {
        String sql = "INSERT INTO ct_encryption_key (user_id, secret_key, private_key, public_key, created_at) VALUES (?, ?, ?, ?, ?)";
        int effect = jdbcTemplate.update(sql, entity.getUserId(), entity.getSecretKey(), entity.getPrivateKey(), entity.getPublicKey(), entity.getCreatedAt());
        return effect > 0;
    }

    public EncryptionKey findByUser(String userId) throws DataAccessException {
        String sql = "SELECT * FROM ct_encryption_key WHERE user_id = ? LIMIT 1";
        return jdbcTemplate.query(sql, rs -> {
            List<EncryptionKey> encKeys = map(rs, EncryptionKey.class);
            return encKeys.isEmpty() ? null : encKeys.get(0);
        }, userId);
    }
}
