package com.sparta.taptoon.global.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriptionManager {

    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final MessageListenerAdapter messageListenerAdapter; // 미리 생성된 리스너 사용

    // 각 채팅방별 Redis 채널을 관리하는 Map
    private final Map<Long, ChannelTopic> chatRoomTopics = new ConcurrentHashMap<>();

    /**
     * 채팅방 구독 메서드
     * 
     * ChatRoomService 에서 채팅방 만들때 호출
     */
    public void subscribeChatRoom(Long chatRoomId) {
        // 채널이름
        // ex) chatroom-1
        String topicName = "chatroom-" + chatRoomId;

        /**
         * 채팅방이 이미 구독되어있는지 확인후
         * 새로운 ChannelTopic을 생성하여 redisMessageListenerContainer에 추가
         * 해당 채팅방 ID와 채널 정보를 chatRoomTopics 맵에 저장하여 관리
         */
        if (!chatRoomTopics.containsKey(chatRoomId)) {
            ChannelTopic topic = new ChannelTopic(topicName);
            redisMessageListenerContainer.addMessageListener(messageListenerAdapter, topic); // 기존 리스너 사용
            chatRoomTopics.put(chatRoomId, topic);
            log.info("채팅방 {} 의 Redis 구독 추가: {}", chatRoomId, topicName);
        }
    }
}



