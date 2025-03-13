package com.conceptune.connect.services;

import com.conceptune.connect.utils.Response;
import com.conceptune.connect.database.models.SignalDevice;
import com.conceptune.connect.database.models.SignalPreKey;
import com.conceptune.connect.database.repository.SignalPreKeyRepository;
import com.conceptune.connect.database.repository.SignalDeviceRepository;
import com.conceptune.connect.exceptions.SignalBundleException;
import com.conceptune.connect.dto.internal.PreKey;
import com.conceptune.connect.dto.request.SignalPreKeyBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class SignalService {

    @Autowired
    private SignalDeviceRepository signalDeviceRepository;

    @Autowired
    private SignalPreKeyRepository signalPreKeyRepository;

    public Long retrieveDeviceId(String userId) {
        return signalDeviceRepository.findIdByUser(userId);
    }

    @Transactional
    public Response<?> retrieveSignalPreKeyBundle(String userId) {
        SignalDevice device = signalDeviceRepository.findByUser(userId);

        if (device == null) {
            return Response.error("No PreKey bundle found for this user.");
        }

        SignalPreKey signalPreKey = signalPreKeyRepository.findOneBySignalDevice(device.getId());

        if (signalPreKey == null) {
            return Response.error("Key shortage for this user.");
        }

        PreKey preKey = new PreKey();
        preKey.setId(signalPreKey.getKeyId());
        preKey.setKey(signalPreKey.getPreKey());

        SignalPreKeyBundle bundle = new SignalPreKeyBundle();
        bundle.setDeviceId(device.getId());
        bundle.setRegistrationId(device.getRegistrationId());
        bundle.setIdentityKey(device.getIdentityKey());
        bundle.setSignedPreKey(device.getSignedPreKey());
        bundle.setSignature(device.getSignature());
        bundle.setPreKeys(List.of(preKey));

        signalPreKeyRepository.deleteBySignalDeviceAndKey(device.getId(), signalPreKey.getKeyId());
        signalDeviceRepository.updatePreKeyCount(device.getId(), device.getPreKeyCount() - 1, Timestamp.from(Instant.now())); 
        return Response.success("Signal pre-key bundle.", bundle);
    }

    /**
     * Register for new signal device if not exists
     * @param userId User ID
     * @param bundle Signal PreKey Bundle Model
     * @return Http Response
     * @throws Exception if an error occurs
     */
    @Transactional
    public Response<?> registerSignalDevice(String userId, SignalPreKeyBundle bundle) throws Exception {
        SignalDevice device = signalDeviceRepository.findByUser(userId);

        if (device != null) {
            boolean isOldSignalDeviceDeleted = signalDeviceRepository.deleteByUser(userId);
            boolean isSignalPreKeyDeleted = signalPreKeyRepository.deleteBySignalDevice(device.getId());

            if (!isOldSignalDeviceDeleted || !isSignalPreKeyDeleted) {
                throw new SignalBundleException("Unable to register signal device.");
            }
        }

        Timestamp currentTimestamp = Timestamp.from(Instant.now());

        SignalDevice signalDevice = new SignalDevice();
        signalDevice.setId(bundle.getDeviceId());
        signalDevice.setUserId(userId);
        signalDevice.setRegistrationId(bundle.getRegistrationId());
        signalDevice.setIdentityKey(bundle.getIdentityKey());
        signalDevice.setSignedPreKey(bundle.getSignedPreKey());
        signalDevice.setSignature(bundle.getSignature());
        signalDevice.setPreKeyCount(bundle.getPreKeys().size());
        signalDevice.setRefreshAt(Timestamp.valueOf(currentTimestamp.toLocalDateTime().plusMonths(6)));
        signalDevice.setUpdatedAt(currentTimestamp);
        signalDevice.setCreatedAt(currentTimestamp);

        boolean isSignalDeviceCreated = signalDeviceRepository.save(signalDevice);
        boolean isPreKeyAdded = addPreKeys(userId, bundle);

        if (isSignalDeviceCreated && isPreKeyAdded) {
            return Response.success("Signal device registered successfully.");
        }

        throw new SignalBundleException("Unable to register signal device.");
    }

    /**
     * Refresh Signal device with new Signal PreKey bundle
     * @param userId User ID
     * @param bundle Signal PreKey Bundle
     * @return Http Response
     * @throws Exception if an error occurs
     */
    @Transactional
    public Response<?> refreshSignalDevice(String userId, SignalPreKeyBundle bundle) throws Exception {
        SignalDevice device = signalDeviceRepository.findByUser(userId);

        if (device == null) {
            throw new SignalBundleException("Unable to refresh signal device.");
        }

        if (!device.getId().equals(bundle.getDeviceId()) || !device.getRegistrationId().equals(bundle.getRegistrationId()) || !device.getIdentityKey().equals(bundle.getIdentityKey())) {
            throw new SignalBundleException("Unable to refresh signal device.");
        }

        boolean isSignedPreKeyUpdated = signalDeviceRepository.updateSignedPreKey(bundle.getDeviceId(), bundle.getSignedPreKey(), bundle.getSignature(), bundle.getPreKeys().size(), Timestamp.from(Instant.now()), Timestamp.from(Instant.now()));
        boolean isPreviousPreKeyDeleted = signalPreKeyRepository.deleteBySignalDevice(bundle.getDeviceId());
        boolean isNewPreKeyAdded = addPreKeys(userId, bundle);

        if (isSignedPreKeyUpdated && isPreviousPreKeyDeleted && isNewPreKeyAdded) {
            return Response.success("Signal device refreshed successfully.");
        }

        throw new SignalBundleException("Unable to refresh signal device.");
    }

    /**
     * Append PreKeys for existing Signal Device
     * @param userId User ID
     * @param bundle Signal PreKey Bundle
     * @return Http Response
     * @throws Exception if an error occurs
     */
    @Transactional
    public Response<?> appendPreKeys(String userId, SignalPreKeyBundle bundle) throws Exception {
        SignalDevice device = signalDeviceRepository.findByUser(userId);

        if (device == null) {
            throw new SignalBundleException("Unable to append signal pre-keys.");
        }

        if (!device.getId().equals(bundle.getDeviceId()) || !device.getRegistrationId().equals(bundle.getRegistrationId()) || !device.getIdentityKey().equals(bundle.getIdentityKey()) || !device.getSignedPreKey().equals(bundle.getSignedPreKey())) {
            throw new SignalBundleException("Unable to append signal pre-keys.");
        }

        boolean isPreKeyCountUpdated = signalDeviceRepository.updatePreKeyCount(device.getId(), device.getPreKeyCount() + bundle.getPreKeys().size(), Timestamp.from(Instant.now()));
        boolean isNewPreKeyAdded = addPreKeys(userId, bundle);

        if (isPreKeyCountUpdated && isNewPreKeyAdded) {
            return Response.success("Signal pre-keys appended successfully.");
        }

        throw new SignalBundleException("Unable to append signal pre-keys.");
    }

    /**
     * Common Function to append PreKeys
     * @param userId user-id
     * @param bundle Signal PreKey Bundle
     * @return boolean
     * @throws Exception if an error occurs
     */
    private boolean addPreKeys(String userId, SignalPreKeyBundle bundle) throws Exception {
        Timestamp createdAt = Timestamp.from(Instant.now());

        List<SignalPreKey> signalPreKeys = bundle.getPreKeys().stream().map(preKey -> {
            SignalPreKey signalPreKey = new SignalPreKey();
            signalPreKey.setUserId(userId);
            signalPreKey.setSignalDeviceId(bundle.getDeviceId());
            signalPreKey.setKeyId(preKey.getId());
            signalPreKey.setPreKey(preKey.getKey());
            signalPreKey.setCreatedAt(createdAt);
            return signalPreKey;
        }).toList();

        return signalPreKeyRepository.batchSave(signalPreKeys);
    }
}
