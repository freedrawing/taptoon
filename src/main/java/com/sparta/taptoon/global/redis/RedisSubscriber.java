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
     * Redis에서 메시지를 수신했을 때 호출되는 메서드
     */
    public void onMessage(String message) {
        try {
            validateMessage(message);
            log.info("📥 Redis 메시지 수신: {}", message);

            Map<String, Object> data = objectMapper.readValue(message, Map.class);
            MessageData parsedData = parseMessageData(data);

            // 필수 필드 검증
            if (parsedData.chatRoomId == null || parsedData.senderId == null) {
                log.error("❌ 메시지 데이터 불완전: chatRoomId={}, senderId={}", parsedData.chatRoomId, parsedData.senderId);
                return;
            }

            // 메시지 타입 구별 및 표시 메시지 결정
            String displayMessage = determineDisplayMessage(parsedData.textMessage, parsedData.thumbnailImageUrl, parsedData.originalImageUrl);
            if (displayMessage == null) {
                log.error("❌ 메시지 데이터 불완전: chatRoomId={}, senderId={}, message=null, imageUrl=null",
                        parsedData.chatRoomId, parsedData.senderId);
                return;
            }

            // 알림 전송
            log.info("📩 메시지 처리: chatRoomId={}, senderId={}, displayMessage={}",
                    parsedData.chatRoomId, parsedData.senderId, displayMessage);
            notificationService.notifyNewMessage(parsedData.chatRoomId, parsedData.senderId, displayMessage);
            log.info("✅ NotificationService로 알림 전송: chatRoomId={}, senderId={}",
                    parsedData.chatRoomId, parsedData.senderId);

            // WebSocket으로 브로드캐스트
            webSocketHandler.broadcastMessage(parsedData.chatRoomId, message);
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

    // 메시지 타입에 따라 표시 메시지 결정
    private String determineDisplayMessage(String textMessage, String thumbnailImageUrl, String originalImageUrl) {
        if (textMessage != null && thumbnailImageUrl == null && originalImageUrl == null) {
            return textMessage; // 텍스트만 있는 경우
        } else if (textMessage == null && (thumbnailImageUrl != null || originalImageUrl != null)) {
            return "이미지 메시지가 도착했습니다."; // 이미지만 있는 경우
        } else if (textMessage != null && (thumbnailImageUrl != null || originalImageUrl != null)) {
            return textMessage + " (이미지 포함)"; // 혼합 메시지
        } else {
            return null; // 텍스트도 이미지도 없는 경우
        }
    }

    // 메시지 데이터를 파싱하여 구조화된 객체로 반환
    private MessageData parseMessageData(Map<String, Object> data) {
        return new MessageData(
                getString(data, "chat_room_id"),
                getLong(data, "sender_id"),
                getString(data, "message"),
                getString(data, "thumbnail_image_url"),
                getString(data, "original_image_url")
        );
    }

    // Map에서 String 값 추출
    private String getString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    // Map에서 Long 값 추출 (타입 안전성 강화)
    private Long getLong(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Invalid Long value for key {}: {}", key, value);
            return null;
        }
    }

    // 메시지 데이터를 담는 내부 레코드
    private record MessageData(
            String chatRoomId,
            Long senderId,
            String textMessage,
            String thumbnailImageUrl,
            String originalImageUrl
    ) {}
}

