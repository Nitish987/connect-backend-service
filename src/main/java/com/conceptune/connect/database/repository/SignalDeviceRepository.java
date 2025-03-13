package com.conceptune.connect.database.repository;

import com.conceptune.connect.database.models.SignalDevice;
import com.conceptune.connect.database.orm.AbstractRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Repository
public class SignalDeviceRepository extends AbstractRepository<SignalDevice> {

    public boolean save(SignalDevice reg) throws DataAccessException {
        String sql = "INSERT INTO ct_signal_device (id, user_id, registration_id, signed_pre_key, signature, identity_key, pre_key_count, refresh_at, updated_at, created_at) VALUES (?,?,?,?,?,?,?,?,?,?)";
        int effect = jdbcTemplate.update(sql, reg.getId(), reg.getUserId(), reg.getRegistrationId(), reg.getSignedPreKey(), reg.getSignature(), reg.getIdentityKey(), reg.getPreKeyCount(), reg.getRefreshAt(), reg.getUpdatedAt(), reg.getCreatedAt());
        return effect > 0;
    }

    public boolean updateSignedPreKey(Long id, String signedPreKey, String signature, Integer preKeyCount, Timestamp refreshAt, Timestamp updatedAt) throws DataAccessException {
        String sql = "UPDATE ct_signal_device SET signed_pre_key = ?, signature = ?, pre_key_count = ?, refresh_at = ?, updated_at = ? WHERE id = ?";
        int effect = jdbcTemplate.update(sql, signedPreKey, signature, preKeyCount, refreshAt, updatedAt, id);
        return effect > 0;
    }

    public boolean updatePreKeyCount(Long id, Integer preKeyCount, Timestamp updatedAt) throws DataAccessException {
        String sql = "UPDATE ct_signal_device SET pre_key_count = ?, updated_at = ? WHERE id = ?";
        int effect = jdbcTemplate.update(sql, preKeyCount, updatedAt, id);
        return effect > 0;
    }

    public SignalDevice findById(Long id) throws DataAccessException {
        String sql = "SELECT * FROM ct_signal_device WHERE id = ? LIMIT 1";
        return jdbcTemplate.query(sql, rs -> {
            List<SignalDevice> signalRegs = map(rs, SignalDevice.class);
            return signalRegs.isEmpty() ? null : signalRegs.get(0);
        }, id);
    }

    public SignalDevice findByUser(String userId) throws DataAccessException {
        String sql = "SELECT * FROM ct_signal_device WHERE user_id = ? LIMIT 1";
        return jdbcTemplate.query(sql, rs -> {
            List<SignalDevice> signalDevices = map(rs, SignalDevice.class);
            return signalDevices.isEmpty() ? null: signalDevices.get(0);
        }, userId);
    }

    public Long findIdByUser(String userId) throws DataAccessException {
        String sql = "SELECT id FROM ct_signal_device WHERE user_id = ? LIMIT 1";
        return jdbcTemplate.query(sql, rs -> {
            List<Long> signalDeviceIds = new ArrayList<>();
            while (rs.next()) {
                signalDeviceIds.add(rs.getLong("id"));
            }
            return signalDeviceIds.isEmpty() ? null: signalDeviceIds.get(0);
        }, userId);
    }

    public boolean deleteByUser(String userId) throws DataAccessException {
        String sql = "DELETE FROM ct_signal_device WHERE user_id = ?";
        int effect = jdbcTemplate.update(sql, userId);
        return effect > 0;
    }
}
