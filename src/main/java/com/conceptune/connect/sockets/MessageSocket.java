package com.conceptune.connect.sockets;

import com.conceptune.connect.broker.BrokerRegistrar;
import com.conceptune.connect.security.common.tokens.JsonWebToken;
import com.conceptune.connect.storages.MessageSessionStorage;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Mono;

import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
public class MessageSocket implements WebSocketHandler {

    private final JsonWebToken jwt;
    private final MessageSessionStorage sessionStorage;
    private final BrokerRegistrar registrar;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String token = session.getHandshakeInfo().getHeaders().getFirst("Authorization");
        UriTemplate template = new UriTemplate("/ws/message/{userId}");
        Map<String, String> variables = template.match(session.getHandshakeInfo().getUri().getPath());
        String userId = variables.get("userId");

        if (!isValidToken(token, userId)) {
            log.debug("Invalid Bearer Token: {}", token);
            return session.close(CloseStatus.BAD_DATA).then(Mono.error(new JwtException("Invalid token")));
        }

        sessionStorage.storeSession(userId, session)
                .doOnSuccess(unused -> {
                    registrar.register(userId);
                    log.info("connected userId: {}", userId);
                })
                .doOnError(e -> {
                    registrar.unregister(userId);
                    log.error("error connecting user: {}: {}", userId, e);
                })
                .subscribe();

        return session.receive().flatMap(webSocketMessage ->  {
            String payload = webSocketMessage.getPayloadAsText();
            return session.send(Mono.just(session.textMessage(payload)));
        }).then();
    }

    private boolean isValidToken(String token, String userId) {
        try {
            if (token == null || !token.startsWith("Bearer ") || userId == null) {
                return false;
            }

            token = token.substring(7);
            Claims claims = jwt.validate(JsonWebToken.ACCESS, token);

            return userId.equals(claims.getSubject());
        } catch (JwtException e) {
            return false;
        }
    }
}
