package com.conceptune.connect.services;

import com.conceptune.connect.constants.ConnectType;
import com.conceptune.connect.database.repository.GroupMemberRepository;
import com.conceptune.connect.database.repository.MessageTokenRepository;
import com.conceptune.connect.broker.dto.MultiMessage;
import com.conceptune.connect.dto.request.NewMessageToken;
import com.conceptune.connect.exceptions.MessagingException;
import com.conceptune.connect.broker.BrokerProducer;
import com.conceptune.connect.utils.Response;
import com.conceptune.connect.broker.dto.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class MessagingService {

    private final GroupMemberRepository groupMemberRepository;
    private final MessageTokenRepository messageTokenRepository;
    private final BrokerProducer brokerProducer;

    /**
     * Publish message to the broker so that message is routed to the particular user, group or any other channels
     *
     * @param message Message to be sent
     * @return Response model
     * @throws Exception if an error occurs
     */
    public Response<?> publishMessage(Message message) throws Exception {
        // for self
        if (ConnectType.valueOf(message.getConnectType()).equals(ConnectType.SELF)) {
            brokerProducer.produce(message, false);
            log.info("published message payload to user itself: {}", message.getRecipientId());
            return Response.success("Message sent successfully.");
        }

        // for contact
        if (ConnectType.valueOf(message.getConnectType()).equals(ConnectType.CONTACT)) {
            brokerProducer.produce(message, true);
            log.info("published message payload to user: {}", message.getRecipientId());
            return Response.success("Message sent successfully.");
        }

        // for group
        if (ConnectType.valueOf(message.getConnectType()).equals(ConnectType.GROUP)) {
            List<String> groupMemberIds = groupMemberRepository.findMemberIdsFromCache(message.getConnectId());

            if (message.getRecipientId().equals("ALL")) {
                for (String memberId : groupMemberIds) {
                    message.setRecipientId(memberId);

                    if (!memberId.equals(message.getSenderId())) {
                        brokerProducer.produce(message, true);
                        log.info("published message payload to [ALL] group {} - user: {}", message.getConnectId(), memberId);
                    }
                }
            } else {

                if (!message.getRecipientId().equals(message.getSenderId())) {
                    brokerProducer.produce(message, true);
                    log.info("published message payload to [Single] group {} - user: {}", message.getConnectId(), message.getRecipientId());
                }
            }

            return Response.success("Message sent successfully.");
        }

        log.error("Invalid connectType found, for publishing message.");
        return Response.success("Unable to publish message.");
    }

    /**
     * Publish multiple messages to a single destination, can be user, group or any other channel
     *
     * @param multiMessage Messages to be sent
     * @return Response model
     * @throws Exception if an error occurs
     */
    public Response<?> publishMultiMessage(MultiMessage multiMessage) throws Exception {
        try {
            for (Message message : multiMessage.getMessages()) {
                publishMessage(message);
            }

            return Response.success("Message sent successfully.");
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    public Response<?> updateMessageToken(String userId, NewMessageToken messageToken) {
        messageTokenRepository.updateToken(userId, messageToken.getToken(), Timestamp.from(Instant.now()));
        return Response.success("Message token updated successfully.");
    }
}
