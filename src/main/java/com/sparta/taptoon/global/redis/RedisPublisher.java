package com.sparta.taptoon.global.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPublisher {

    private final StringRedisTemplate redisTemplate;

    /**
     * publish (Redis 메시지 발행)
     */
    public void publish(Long chatRoomId, String message) {
        if (chatRoomId == null) {
            log.error("Redis 메시지 발행 실패: 채팅방 ID가 null입니다.");
            return;
        }

        // Redis Pub/Sub에서 사용할 채널을 설정
        String topic = "chatroom-" + chatRoomId;
        log.info("Redis에 메시지 발행 시도 (채널: {}): {}", topic, message);

        try {
            // 메시지를 해당 채널에 발행
            redisTemplate.convertAndSend(topic, message);
            log.info("Redis 메시지 발행 완료 (채널: {}): {}", topic, message);
        } catch (Exception e) {
            log.error("Redis 메시지 발행 중 오류 발생 (채널: {}): {}", topic, e.getMessage(), e);
        }
    }
}

