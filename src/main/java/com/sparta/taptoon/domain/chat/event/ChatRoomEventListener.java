package com.sparta.taptoon.domain.chat.event;

import com.sparta.taptoon.global.redis.RedisSubscriptionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomEventListener {

    private final RedisSubscriptionManager redisSubscriptionManager;

    @EventListener
    public void handleChatRoomCreated(ChatRoomCreatedEvent event) {
        redisSubscriptionManager.subscribeChatRoom(event.getChatRoomId());
        log.info("✅ Redis 채널 구독 - chatRoomId: {}", event.getChatRoomId());
    }
}
