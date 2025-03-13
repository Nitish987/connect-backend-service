package com.conceptune.connect.broker.core;

import com.conceptune.connect.broker.dto.Message;
import com.conceptune.connect.broker.utils.IBrokerConsumer;
import com.conceptune.connect.settings.InstanceVariable;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
public class Broker {

    private final ObjectMapper MAPPER = new ObjectMapper();
    private final List<IBrokerConsumer> consumers = new ArrayList<>();

    private ReactorNettyWebSocketClient socket;
    private WebSocketSession session;
    private WebClient http;
    private String accessToken;
    private String refreshToken;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${connect.broker.server}")
    private String BROKER_SERVER;

    @Value("${connect.broker.username}")
    private String BROKER_USERNAME;

    @Value("${connect.broker.password}")
    private String BROKER_PASSWORD;

    @Value("${connect.broker.ssl}")
    private String BROKER_SSL;

    @PostConstruct
    private void init() {
        createWebClients();
        authorizeBroker();
    }

    /**
     * Create reactor web http and socket clients
     */
    private void createWebClients() {
        socket = new ReactorNettyWebSocketClient();

        String protocol = BROKER_SSL.equals("true") ? "https" : "http";
        String url = String.format("%s://%s/api", protocol, BROKER_SERVER);
        http = webClientBuilder.baseUrl(url).filter(onRequestExchange()).filter(onResponseExchange()).build();
    }

    /**
     * Http request interceptor for broker
     *
     * @return Request Exchange Filter Function
     */
    private ExchangeFilterFunction onRequestExchange() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (accessToken != null) {
                ClientRequest authorizedRequest = ClientRequest.from(clientRequest)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .build();
                return Mono.just(authorizedRequest);
            }
            return Mono.just(clientRequest);
        });
    }

    /**
     * Http response interceptor for broker
     *
     * @return Response Exchange Filter Function
     */
    private ExchangeFilterFunction onResponseExchange() {
        return (request, next) -> next.exchange(request).flatMap(clientResponse -> {
            if (clientResponse.statusCode().value() == 401) {
                return refreshAuthToken().flatMap(token -> {
                    ClientRequest authorizedRequest = ClientRequest.from(request).headers(headers -> {
                        headers.remove(HttpHeaders.AUTHORIZATION);
                        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                    }).build();

                    return next.exchange(authorizedRequest);
                }).onErrorResume(e -> {
                    log.error("Error during token refresh and request retry: {}", e.getMessage());
                    return Mono.just(clientResponse);
                });
            }
            return Mono.just(clientResponse);
        });
    }

    /**
     * Refresh token api
     *
     * @return Unused response string
     */
    private Mono<String> refreshAuthToken() {
        return http.post()
                .uri("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("token", refreshToken))
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        Map<String, Object> serializedResponse = MAPPER.readValue(response, Map.class);
                        boolean isSuccess = (boolean) serializedResponse.get("success");
                        Map<String, String> data = (Map<String, String>) serializedResponse.get("data");

                        if (isSuccess) {
                            accessToken = data.get("_at");
                            refreshToken = data.get("_rt");

                            log.info("Broker refreshed successfully.");

                            return accessToken;
                        } else {
                            log.error("Error while refreshing broker token: {}", serializedResponse.get("message"));
                        }

                    } catch (Exception e) {
                        log.error("Error in refreshing broker token: {}", e.getMessage());
                    }

                    return "";
                }).onErrorResume(e -> {
                    log.error("Unable to refresh token: {}", e.getMessage());
                    return Mono.just("");
                });
    }

    /**
     * Broker authorization api
     */
    private void authorizeBroker() {
        http.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "instanceId", InstanceVariable.ID,
                        "username", BROKER_USERNAME,
                        "password", BROKER_PASSWORD
                ))
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> {
                    try {
                        Map<String, Object> serializedResponse = MAPPER.readValue(response, Map.class);
                        boolean isSuccess = (boolean) serializedResponse.get("success");
                        Map<String, String> data = (Map<String, String>) serializedResponse.get("data");

                        if (isSuccess) {
                            this.accessToken = data.get("_at");
                            this.refreshToken = data.get("_rt");

                            log.info("Broker authorized successfully.");

                            // create broker websocket connection after authorization
                            createBrokerSocketConnection();
                        } else {
                            log.error("Error while authorizing broker: {}", serializedResponse.get("message"));
                        }

                    } catch (Exception e) {
                        log.error("Error in authorizing broker: {}", e.getMessage());

                    }
                }).onErrorResume(e -> {
                    log.error("Unable to authorize broker: {}", e.getMessage());
                    return Mono.empty();
                }).subscribe();
    }

    /**
     * Create broker websocket connection for payload consuming
     */
    private void createBrokerSocketConnection() {
        String protocol = BROKER_SSL.equals("true") ? "wss" : "ws";
        String url = String.format("%s://%s/ws/b/%s", protocol, BROKER_SERVER, InstanceVariable.ID);
        URI uri = URI.create(url);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        this.socket.execute(uri, headers, session -> {
            this.session = session;

            log.info("Broker consumer connection established.");

            return session.receive().flatMap(webSocketMessage -> {
                String payload = webSocketMessage.getPayloadAsText();
                try {
                    Message message = MAPPER.readValue(payload, Message.class);

                    for (IBrokerConsumer consumer : consumers) {
                        consumer.consume(message);
                    }
                } catch (Exception e) {
                    log.error("Error while parsing broker consumed payload: {}", e.getMessage());
                }

                return Mono.empty();
            }).then();
        }).subscribe();
    }

    /**
     * Add consumer to the broker
     *
     * @param consumer Consumer Function
     */
    public void addConsumer(IBrokerConsumer consumer) {
        consumers.add(consumer);
    }

    /**
     * Publish message to broker
     *
     * @param message Broker message
     * @return Boolean - true if success else false
     */
    public Mono<Boolean> publish(Message message) {
        return http.post()
                .uri("/producer/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(message.toJsonString())
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        Map<String, Object> serializedResponse = MAPPER.readValue(response, Map.class);
                        boolean isSuccess = (boolean) serializedResponse.get("success");

                        if (isSuccess) {
                            log.info("Sent Message to broker.");
                        } else {
                            log.error("Unable to Sent Message to broker: {}", serializedResponse.get("message"));
                        }

                        return isSuccess;

                    } catch (Exception e) {
                        log.error("Error while publishing message to broker: {}", e.getMessage());
                    }

                    return false;

                }).onErrorResume(e -> {
                    log.error("Unable to publish message to broker: {}", e.getMessage());
                    return Mono.just(false);
                });
    }

    public void registerUser(String userId) {
        http.post()
                .uri("/instance/register?userId=" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        Map<String, Object> serializedResponse = MAPPER.readValue(response, Map.class);
                        boolean isSuccess = (boolean) serializedResponse.get("success");

                        if (isSuccess) {
                            log.info("Broker: User [{}] registered successfully.", userId);
                        } else {
                            log.error("Broker: Unable to register user: {}", userId);
                        }

                        return isSuccess;

                    } catch (Exception e) {
                        log.error("Error while registering user to broker: {}", e.getMessage());
                    }

                    return false;

                }).onErrorResume(e -> {
                    log.error("Unable to register user to broker: {}", e.getMessage());
                    return Mono.just(false);
                }).subscribe();
    }

    public void unregisterUser(String userId) {
        http.post()
                .uri("/instance/unregister?userId=" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        Map<String, Object> serializedResponse = MAPPER.readValue(response, Map.class);
                        boolean isSuccess = (boolean) serializedResponse.get("success");

                        if (isSuccess) {
                            log.info("Broker: User [{}] unregistered successfully.", userId);
                        } else {
                            log.error("Broker: Unable to unregister user: {}", userId);
                        }

                        return isSuccess;

                    } catch (Exception e) {
                        log.error("Error while unregistering user to broker: {}", e.getMessage());
                    }

                    return false;

                }).onErrorResume(e -> {
                    log.error("Unable to unregister user to broker: {}", e.getMessage());
                    return Mono.just(false);
                }).subscribe();
    }

    @PreDestroy
    private void dispose() {
        if (this.session != null && this.session.isOpen()) {
            this.session.close();
            log.info("Broker socket connection closed.");
        }
    }
}
