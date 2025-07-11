package com.greenlink.greenlink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.logging.Logger;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = Logger.getLogger(WebSocketConfig.class.getName());

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker for subscription destinations prefixed with /topic
        // Messages whose destination starts with /app will be routed to @MessageMapping methods
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
        logger.info("WebSocket message broker configured");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoints for client connection, enable SockJS fallback
        registry.addEndpoint("/ws-messages").setAllowedOrigins("*").withSockJS();
        logger.info("WebSocket STOMP endpoints registered");
    }
} 