package com.conceptune.connect.storages;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Repository
public class MessageSessionStorage {

    final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public Mono<Void> storeSession(String userId, WebSocketSession session) {
        WebSocketSession previousSession = sessions.put(userId, session);

        if (previousSession != null && previousSession.isOpen()) {
            return previousSession.close(CloseStatus.NOT_ACCEPTABLE);
        }

        return Mono.empty();
    }

    public WebSocketSession retrieveSession(String userId) {
        return sessions.get(userId);
    }

    public Mono<Void> deleteSession(String userId, CloseStatus status) {
        WebSocketSession session = sessions.get(userId);

        if (session != null) {
            return session.close(status).doFinally(signalType -> sessions.remove(userId));
        }

        return Mono.empty();
    }
}
