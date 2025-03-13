package com.conceptune.connect.filters;

import com.conceptune.connect.utils.Response;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Log4j2
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class ClientRequestCheckFilter implements WebFilter {

    @Value("${connect.security.api-key}")
    private String API_KEY;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpHeaders httpHeaders = exchange.getRequest().getHeaders();

        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        log.info("Request [{}]: {}", method, path);

        String apiKey = httpHeaders.getFirst("Cherry");

        if (apiKey == null || !apiKey.equals(API_KEY)) {
            return unauthorizedResponse(exchange, "Unauthorized Client.");
        }

        return chain.filter(exchange);
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
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
