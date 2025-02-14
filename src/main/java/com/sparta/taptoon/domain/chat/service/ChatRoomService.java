package com.sparta.taptoon.domain.chat.service;

import com.sparta.taptoon.domain.chat.dto.request.CreateChatRoomRequest;
import com.sparta.taptoon.domain.chat.dto.response.ChatRoomResponse;
import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.chat.repository.ChatRoomRepository;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.redis.RedisSubscriptionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final RedisSubscriptionManager redisSubscriptionManager;

    public ChatRoomResponse createChatRoom(CreateChatRoomRequest request) {
        Member member1 = memberRepository.findById(request.memberId1())
                .orElseThrow(() -> new RuntimeException("User not Found"));
        Member member2 = memberRepository.findById(request.memberId2())
                .orElseThrow(() -> new RuntimeException("User not Found"));

        Optional<ChatRoom> existingRoom = chatRoomRepository.findByMember1AndMember2(member1, member2);
        if (existingRoom.isPresent()) {
            return ChatRoomResponse.from(existingRoom.get());
        }

        ChatRoom chatRoom = chatRoomRepository.save(request.toEntity(member1, member2));

        // 채팅방 생성 시 Redis 구독 추가
        redisSubscriptionManager.subscribeChatRoom(chatRoom.getId());

        return ChatRoomResponse.from(chatRoom);
    }
}
