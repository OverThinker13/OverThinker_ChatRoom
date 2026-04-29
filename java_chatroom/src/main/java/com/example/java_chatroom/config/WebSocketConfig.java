package com.example.java_chatroom.config;

import com.example.java_chatroom.controller.WebSocketAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private WebSocketAPI webSocketAPI;

    @Autowired
    private JwtWebSocketInterceptor jwtWebSocketInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketAPI, "/WebSocketMessage")
                .addInterceptors(jwtWebSocketInterceptor);
    }
}
