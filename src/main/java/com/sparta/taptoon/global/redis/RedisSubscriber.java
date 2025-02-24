package com.sparta.taptoon.global.redis;

import com.sparta.taptoon.global.handler.WebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

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
        validateMessage(message);
        log.info("📥 Redis 메시지 수신: {}", message);  // 메시지 수신 확인 로그

        try {
            webSocketHandler.broadcastMessage(message);
        } catch (IOException e) {
            log.error("❌ WebSocket 메시지 브로드캐스트 실패: {}", "IO 오류 발생: " + e.getMessage(), e);
            // 호출자에게 전파하지 않고 로깅으로 처리 (필요 시 throw 추가 가능)
        } catch (Exception e) {
            log.error("❌ WebSocket 메시지 브로드캐스트 실패: {}", "예상치 못한 오류 발생: " + e.getMessage(), e);
        }
    }

    // 메시지 유효성 검사
    private void validateMessage(String message) {
        if (webSocketHandler == null) {
            throw new IllegalStateException("WebSocketHandler가 초기화되지 않았습니다.");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("수신된 메시지는 null이거나 비어 있을 수 없습니다.");
        }
    }
}

