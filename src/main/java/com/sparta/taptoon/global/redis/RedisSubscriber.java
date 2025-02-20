package com.sparta.taptoon.global.redis;

import com.sparta.taptoon.global.handler.WebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber {

    private final WebSocketHandler webSocketHandler;

    /**
     * onMessage
     *
     * Redis에서 메시지를 수신했을 때 호출되는 메서드
     */
    public void onMessage(String message) throws Exception {
        log.info("📥 Redis 메시지 수신: {}", message);  // ✅ 메시지 수신 확인 로그

        if (webSocketHandler == null) {
            log.error("❌ WebSocketHandler가 null입니다! 메시지를 처리할 수 없습니다.");
            return;
        }

        log.info("📤 WebSocket으로 메시지 전달 시도!");  // ✅ WebSocket 전달 로그
        webSocketHandler.broadcastMessage(message);
    }
}
