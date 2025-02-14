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

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final RedisPublisher redisPublisher;
    private final ObjectMapper objectMapper;

    @Transactional
    public ChatMessageResponse saveAndPublishMessage(SendChatMessageRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.chatRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));
        Member sender = memberRepository.findById(request.senderId())
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        ChatMessage chatMessage = chatMessageRepository.save(request.toEntity(chatRoom, sender));
        ChatMessageResponse response = ChatMessageResponse.from(chatMessage);

        try {
            // 메시지 저장 후 Redis로 발행
            redisPublisher.publish(chatRoom.getId(), objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            log.error("Redis 메시지 발행 중 오류 발생", e);
        }

        return response;
    }
}