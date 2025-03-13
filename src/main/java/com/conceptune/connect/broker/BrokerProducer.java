package com.conceptune.connect.broker;

import com.conceptune.connect.broker.core.Broker;
import com.conceptune.connect.broker.dto.Message;
import com.conceptune.connect.broker.utils.IBrokerProducer;
import com.conceptune.connect.firebase.FcmService;
import com.conceptune.connect.storages.MessageSessionStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Log4j2
@Component
@RequiredArgsConstructor
public class BrokerProducer implements IBrokerProducer {

    private final MessageSessionStorage sessionStorage;
    private final FcmService fcmService;
    private final Broker broker;

    @Override
    public void produce(Message message, boolean useFallback) {
        WebSocketSession session = sessionStorage.retrieveSession(message.getRecipientId());

        if (session != null && session.isOpen()) {
            // If session is there means user is online on same instance

            session.send(Mono.just(session.textMessage(message.toJsonString())))
                    .doOnSuccess(unused -> log.info("Sent Message through websocket to: [{}]", message.getRecipientId()))
                    .doOnError(e -> {
                        log.error("Unable to Sent Message through websocket to: {}: Error: {}", message.getRecipientId(), e.getMessage());
                        if (useFallback) {
                            sendViaFCM(message);
                        }
                    }).subscribe();
        } else if (useFallback) {

            // sending message to broker for correct message routing
            broker.publish(message).doOnSuccess(isPublished -> {
                if (isPublished != null && !isPublished) {
                    sendViaFCM(message);
                }
            }).doOnError(e -> {
                log.error("Unable to publish Message through broker to: {}: Error: {}", message.getRecipientId(), e.getMessage());
                sendViaFCM(message);
            }).subscribe();
        }
    }

    private void sendViaFCM(Message message) {
        try {
            fcmService.sendBrokerMessage(message);
        } catch (Exception e) {
            log.error("Error while sending via FCM: {}", e.getMessage());
        }
    }
}
