package com.sparta.taptoon.global.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.taptoon.global.handler.NotificationService;
import com.sparta.taptoon.global.handler.WebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber {

    private final WebSocketHandler webSocketHandler;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            Map<String, Object> data = objectMapper.readValue(message, Map.class);
            String chatRoomId = data.get("chat_room_id") != null ? data.get("chat_room_id").toString() : null;
            Long senderId = data.get("sender_id") != null ? Long.valueOf(data.get("sender_id").toString()) : null;
            String content = (String) data.get("message");

            if (chatRoomId == null || senderId == null || content == null) {
                log.error("❌ 메시지 데이터 불완전: chatRoomId={}, senderId={}, message={}", chatRoomId, senderId, content);
                return;
            }

            notificationService.notifyNewMessage(chatRoomId, senderId, content);
            log.info("✅ NotificationWebSocketHandler로 알림 전송: chatRoomId={}, senderId={}", chatRoomId, senderId);
        } catch (IOException e) {
            log.error("❌ WebSocket 메시지 브로드캐스트 실패: {}", "IO 오류 발생: " + e.getMessage(), e);
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

