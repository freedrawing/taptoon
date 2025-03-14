package com.sparta.taptoon.global.config;

import com.sparta.taptoon.global.handler.NotificationWebSocketHandler;
import com.sparta.taptoon.global.interceptor.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/chat/{roomId}")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*"); // CORS 문제 해결

        registry.addHandler(notificationWebSocketHandler, "/notifications/{userId}")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*");
    }
}
