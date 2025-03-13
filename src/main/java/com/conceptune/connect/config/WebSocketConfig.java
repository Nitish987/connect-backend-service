package com.conceptune.connect.config;

import com.conceptune.connect.sockets.MessageSocket;
import com.conceptune.connect.sockets.WebRTCSocket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSocketConfig {

    @Bean
    public HandlerMapping messageMapping(MessageSocket messageSocket) {
        Map<String, WebSocketHandler> mapping = new HashMap<String, WebSocketHandler>();
        mapping.put("/ws/message/{userId}", messageSocket);

        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(1);
        handlerMapping.setUrlMap(mapping);
        return handlerMapping;
    }

    @Bean
    public HandlerMapping webrtcMapping(WebRTCSocket webRTCSocket) {
        Map<String, WebSocketHandler> mapping = new HashMap<String, WebSocketHandler>();
        mapping.put("/ws/webrtc", webRTCSocket);

        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(2);
        handlerMapping.setUrlMap(mapping);
        return handlerMapping;
    }
}
