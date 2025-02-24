package com.sparta.taptoon.global.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPublisher {

    private static final String CHANNEL_PREFIX = "chatroom-"; // 채널 이름 접두사

    private final StringRedisTemplate redisTemplate;

    /**
     * publish (Redis 메시지 발행)
     */
    public void publish(Long chatRoomId, String message) {
        validateInput(chatRoomId, message);
        String topic = createChannelName(chatRoomId);

        log.info("Redis에 메시지 발행 시도 (채널: {}): {}", topic, message);
        try {
            redisTemplate.convertAndSend(topic, message);
            log.info("Redis 메시지 발행 완료 (채널: {}): {}", topic, message);
        } catch (RedisSystemException e) {
            log.error("Redis 메시지 발행 실패 (채널: {}): {}", topic, "Redis 연결 오류: " + e.getMessage(), e);
            throw e; // 호출자에게 예외 전파
        } catch (Exception e) {
            log.error("Redis 메시지 발행 실패 (채널: {}): {}", topic, "예상치 못한 오류: " + e.getMessage(), e);
            throw new RuntimeException("Redis 메시지 발행 중 오류 발생", e);
        }
    }

    // 입력값 유효성 검사
    private void validateInput(Long chatRoomId, String message) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("채팅방 ID는 null 일 수 없습니다.");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("메시지는 null 이거나 비어 있을 수 없습니다.");
        }
    }

    // 채널 이름 생성
    private String createChannelName(Long chatRoomId) {
        return CHANNEL_PREFIX + chatRoomId;
    }
}

