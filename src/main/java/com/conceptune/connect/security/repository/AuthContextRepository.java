package com.conceptune.connect.security.repository;

import com.conceptune.connect.security.common.tokens.JsonWebToken;
import com.conceptune.connect.utils.Response;
import com.conceptune.connect.security.dto.Auth;
import io.jsonwebtoken.Claims;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Log4j2
@Component
public class AuthContextRepository implements ServerSecurityContextRepository {

    @Autowired
    private JsonWebToken jwt;

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        HttpHeaders httpHeaders = exchange.getRequest().getHeaders();
        String path = exchange.getRequest().getPath().value();

        if (path.startsWith("/api/auth") || path.startsWith("/ws")) {
            return Mono.empty();
        }

        try {
            String token = httpHeaders.getFirst(HttpHeaders.AUTHORIZATION);
            if (token == null || !token.startsWith("Bearer ")) {
                log.debug("Invalid Bearer Token: {}", token);
                return unauthorizedResponse(exchange, "Invalid credentials or credentials not provided.");
            }

            token = token.substring(7);
            Claims claims = jwt.validate(JsonWebToken.ACCESS, token);
            Auth auth = new Auth(token, claims.getSubject(), claims.get("username").toString(), Map.of("country", claims.get("country")));
            auth.setAuthenticated(true);

            return Mono.just(new SecurityContextImpl(auth));
        } catch (Exception e) {
            log.error("Authentication Error: {}", e.getMessage());
            return unauthorizedResponse(exchange, "Unauthorized");
        }
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        throw new UnsupportedOperationException("Not supported or Implemented.");
    }

    private Mono<SecurityContext> unauthorizedResponse(ServerWebExchange exchange, String message) {
        // checking whether the response is already sent to client
        if (exchange.getResponse().isCommitted()) {
            return Mono.empty();
        }

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String responseBody = Response.error(message).toJsonString();
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(responseBody.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer)).then(Mono.empty());
    }
}
