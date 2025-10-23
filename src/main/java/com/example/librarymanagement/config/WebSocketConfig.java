package com.example.librarymanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        /*
         * /topic → dùng cho broadcast (gửi cho nhiều người)
         * /queue → dùng cho message riêng (point-to-point)
         * */
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix for app destinations
        config.setApplicationDestinationPrefixes("/app");
        // Prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    // Khai báo endpoint mà client sẽ kết nối WebSocket tới backend
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }


    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(preses)
    }
}
