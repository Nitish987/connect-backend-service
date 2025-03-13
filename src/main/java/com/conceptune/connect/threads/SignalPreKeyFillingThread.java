package com.conceptune.connect.threads;

import com.conceptune.connect.database.repository.MessageTokenRepository;
import com.conceptune.connect.firebase.dto.FcmMessage;
import com.conceptune.connect.firebase.FcmService;
import com.conceptune.connect.constants.FcmQuery;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Log4j2
@Component
@ConditionalOnProperty(name = "connect.threads.signal-pre-key-thread", havingValue = "true")
public class SignalPreKeyFillingThread {

    @Autowired
    private MessageTokenRepository messageTokenRepository;

    @Autowired
    private FcmService fcmService;

    @Scheduled(cron = "0 */3 * * * ?")
    public void run() {
        try {
            List<String> tokens = messageTokenRepository.findAllTokensHavingSignalPreKeyShortage(50);

            if (!tokens.isEmpty()) {
                FcmMessage fcmMessage = FcmMessage.builder().query(FcmQuery.PRE_KEY_FILLING).message("Signal pre-key required.").data(Map.of()).build();
                fcmService.send(tokens, fcmMessage);
                log.info("Sent Signal Pre-Key Filling Reminder to user.");
            } else {
                log.info("No user found with Signal Pre-Key shortage.");
            }
        } catch (Exception e) {
            log.error("Failed to remind for Pre-key Filling.", e);
        }
    }
}
