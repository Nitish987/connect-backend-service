package com.conceptune.connect.broker;

import com.conceptune.connect.broker.core.Broker;
import com.conceptune.connect.broker.dto.Message;
import com.conceptune.connect.broker.utils.IBrokerConsumer;

import com.conceptune.connect.firebase.FcmService;
import com.conceptune.connect.storages.MessageSessionStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Log4j2
@Component
@RequiredArgsConstructor
public class BrokerConsumer implements IBrokerConsumer {

    private final MessageSessionStorage sessionStorage;
    private final FcmService fcmService;
    private final Broker broker;

    private final ObjectMapper MAPPER = new ObjectMapper();

    @PostConstruct
    private void consume() {
        broker.addConsumer(this);
    }

    @Override
    public void consume(Message message) {
        try {
            log.info("consumed message payload for user: {}", message.getRecipientId());

            WebSocketSession session = sessionStorage.retrieveSession(message.getRecipientId());

            if (session != null && session.isOpen()) {
                // If session is there means user is online and sending message through websocket

                session.send(Mono.just(session.textMessage(message.toJsonString())))
                        .doOnSuccess(unused -> log.info("Sent Message through websocket to: [{}]", message.getRecipientId()))
                        .doOnError(e -> {
                            log.error("Unable to Sent Message to: {}: Error: {}", message.getRecipientId(), e.getMessage());
                            try {
                                fcmService.sendBrokerMessage(message);
                            } catch (Exception ignored) {}
                        }).subscribe();
            } else {
                // sending via fcm
                fcmService.sendBrokerMessage(message);
            }
        } catch (Exception e) {
            log.error("Consumer Error: {}", e.getMessage());
        }
    }
}
