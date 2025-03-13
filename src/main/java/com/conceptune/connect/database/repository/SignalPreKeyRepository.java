package com.conceptune.connect.database.repository;

import com.conceptune.connect.database.models.SignalPreKey;
import com.conceptune.connect.database.orm.AbstractRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Repository
public class SignalPreKeyRepository extends AbstractRepository<SignalPreKey> {

    public boolean save(SignalPreKey entity) throws DataAccessException {
        String sql = "INSERT INTO ct_signal_pre_keys (user_id, signal_device_id, key_id, pre_key, created_at) VALUES (?,?,?,?,?)";
        int effect = jdbcTemplate.update(sql, entity.getUserId(), entity.getSignalDeviceId(), entity.getKeyId(), entity.getPreKey(), entity.getCreatedAt());
        return effect > 0;
    }

    public boolean batchSave(List<SignalPreKey> preKeys) throws DataAccessException {
        String sql = "INSERT INTO ct_signal_pre_keys (user_id, signal_device_id, key_id, pre_key, created_at) VALUES (?,?,?,?,?)";
        int[][] effect = jdbcTemplate.batchUpdate(sql, preKeys, 20, (ps, preKey) -> {
            ps.setString(1, preKey.getUserId());
            ps.setLong(2, preKey.getSignalDeviceId());
            ps.setLong(3, preKey.getKeyId());
            ps.setString(4, preKey.getPreKey());
            ps.setTimestamp(5, preKey.getCreatedAt());
        });
        return effect.length > 0;
    }

    public SignalPreKey findOneBySignalDevice(Long signalDeviceId) throws DataAccessException {
        String sql = "SELECT * FROM ct_signal_pre_keys WHERE signal_device_id = ? LIMIT 1";
        return jdbcTemplate.query(sql, rs -> {
            List<SignalPreKey> preKeys = map(rs, SignalPreKey.class);
            return preKeys.isEmpty() ? null : preKeys.get(0);
        }, signalDeviceId);
    }

    public boolean deleteBySignalDevice(Long signalDeviceId) throws DataAccessException {
        String sql = "DELETE FROM ct_signal_pre_keys WHERE signal_device_id = ?";
        int effect = jdbcTemplate.update(sql, signalDeviceId);
        return effect > 0;
    }

    public boolean deleteBySignalDeviceAndKey(Long signalDeviceId, Long preKeyId) throws DataAccessException {
        String sql = "DELETE FROM ct_signal_pre_keys WHERE signal_device_id = ? AND key_id = ?";
        int effect = jdbcTemplate.update(sql, signalDeviceId, preKeyId);
        return effect > 0;
    }
}
