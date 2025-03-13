package com.conceptune.connect.firebase;

import com.conceptune.connect.broker.dto.Message;
import com.conceptune.connect.constants.FcmQuery;
import com.conceptune.connect.database.repository.MessageTokenRepository;
import com.conceptune.connect.firebase.dto.FcmMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class FcmService {

    @Autowired
    private MessageTokenRepository messageTokenRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    public void send(List<String> tokens, FcmMessage fcmMessage) throws Exception {
        MulticastMessage message = MulticastMessage.builder()
                .putData("query", fcmMessage.getQuery().getValue())
                .putData("message", fcmMessage.getMessage())
                .putData("data", mapper.writeValueAsString(fcmMessage.getData()))
                .addAllTokens(tokens)
                .build();

        FirebaseMessaging.getInstance().sendEachForMulticast(message);
    }

    public void sendBrokerMessage(Message message) throws Exception {
        String token = messageTokenRepository.findTokenByUser(message.getRecipientId());
        FcmMessage fcmMessage = FcmMessage.builder().query(FcmQuery.MESSAGE).message("Incoming Message.").data(message.toMap()).build();
        send(List.of(token), fcmMessage);
        log.info("Sent Message through FCM to: [{}]", message.getRecipientId());
    }
}
