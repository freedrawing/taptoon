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
    public void onMessage(String message) {
        try {
            validateMessage(message);
            log.info("📥 Redis 메시지 수신: {}", message);

            Map<String, Object> data = objectMapper.readValue(message, Map.class);
            String chatRoomId = data.get("chat_room_id") != null ? data.get("chat_room_id").toString() : null;
            Long senderId = data.get("sender_id") != null ? Long.valueOf(data.get("sender_id").toString()) : null;
            String textMessage = data.get("message") != null ? data.get("message").toString() : null;
            String imageUrl = data.get("image_url") != null ? data.get("image_url").toString() : null;
            String thumbnailImageUrl = data.get("thumbnail_image_url") != null ? data.get("thumbnail_image_url").toString() : null;
            String originalImageUrl = data.get("original_image_url") != null ? data.get("original_image_url").toString() : null;

            // 필수 필드 검증
            if (chatRoomId == null || senderId == null) {
                log.error("❌ 메시지 데이터 불완전: chatRoomId={}, senderId={}", chatRoomId, senderId);
                return;
            }

            // 메시지 타입 구별
            if (textMessage != null && thumbnailImageUrl == null && originalImageUrl == null) {
                log.info("📝 텍스트 메시지 처리: chatRoomId={}, senderId={}, message={}", chatRoomId, senderId, textMessage);
                notificationService.notifyNewMessage(chatRoomId, senderId, textMessage);
                log.info("✅ NotificationService로 텍스트 알림 전송: chatRoomId={}, senderId={}", chatRoomId, senderId);
            } else if ((thumbnailImageUrl != null || originalImageUrl != null) && textMessage == null) {
                String displayUrl = thumbnailImageUrl != null ? thumbnailImageUrl : originalImageUrl;
                log.info("🖼️ 이미지 메시지 처리: chatRoomId={}, senderId={}, thumbnailImageUrl={}, originalImageUrl={}",
                        chatRoomId, senderId, thumbnailImageUrl, originalImageUrl);
                notificationService.notifyNewMessage(chatRoomId, senderId, "이미지 메시지가 도착했습니다.");
            } else if (textMessage == null && thumbnailImageUrl == null && originalImageUrl == null) {
                log.error("❌ 메시지 데이터 불완전: chatRoomId={}, senderId={}, message=null, imageUrl=null", chatRoomId, senderId);
                return;
            } else {
                log.info("📝🖼️ 혼합 메시지 처리: chatRoomId={}, senderId={}, message={}, thumbnailImageUrl={}, originalImageUrl={}",
                        chatRoomId, senderId, textMessage, thumbnailImageUrl, originalImageUrl);
                notificationService.notifyNewMessage(chatRoomId, senderId, textMessage); // 혼합 시 텍스트 우선
            }

            // WebSocket으로 브로드캐스트
            webSocketHandler.broadcastMessage(chatRoomId, message);
        } catch (IOException e) {
            log.error("❌ 메시지 파싱 실패: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ 메시지 처리 중 예상치 못한 오류: {}", e.getMessage(), e);
        }
    }

    // 메시지 유효성 검사
    private void validateMessage(String message) {
        if (webSocketHandler == null) {
            throw new IllegalStateException("WebSocketHandler가 초기화되지 않았습니다.");
        }
        if (notificationService == null) {
            throw new IllegalStateException("NotificationService가 초기화되지 않았습니다.");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("수신된 메시지는 null이거나 비어 있을 수 없습니다.");
        }
    }
}

