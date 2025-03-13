package com.conceptune.connect.sockets;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component
public class WebRTCSocket implements WebSocketHandler {

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive().flatMap(webSocketMessage -> session.send(Mono.just(webSocketMessage))).then();
    }
}
