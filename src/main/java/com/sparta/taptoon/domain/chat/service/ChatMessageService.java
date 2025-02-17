package com.sparta.taptoon.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.taptoon.domain.chat.dto.request.SendChatMessageRequest;
import com.sparta.taptoon.domain.chat.dto.response.ChatMessageResponse;
import com.sparta.taptoon.domain.chat.entity.ChatMessage;
import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.chat.repository.ChatMessageRepository;
import com.sparta.taptoon.domain.chat.repository.ChatRoomRepository;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.redis.RedisPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final RedisPublisher redisPublisher;
    private final ObjectMapper objectMapper;

    @Transactional
    public ChatMessageResponse sendMessage(Long senderId, SendChatMessageRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.chatRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));
        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        // ✅ 메시지 저장
        ChatMessage chatMessage = chatMessageRepository.save(request.toEntity(chatRoom, sender, request.message()));
        ChatMessageResponse response = ChatMessageResponse.from(chatMessage);

        try {
            // ✅ Redis로 메시지 발행 (WebSocket에서도 받을 수 있도록)
            redisPublisher.publish(chatRoom.getId(), objectMapper.writeValueAsString(response));
            log.info("📤 Redis에 메시지 발행 완료: {}", response);
        } catch (Exception e) {
            log.error("❌ Redis 메시지 발행 중 오류 발생", e);
        }

        return response;
    }
}