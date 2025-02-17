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
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final RedisSubscriptionManager redisSubscriptionManager;

    @Transactional
    public ChatRoomResponse createChatRoom(Long memberId1, CreateChatRoomRequest request) {
        Member member1 = memberRepository.findById(memberId1)
                .orElseThrow(() -> new RuntimeException("요청한 사용자를 찾을 수 없습니다."));
        Member member2 = memberRepository.findById(request.memberId2())
                .orElseThrow(() -> new RuntimeException("대화 상대를 찾을 수 없습니다."));

        // 이미 존재하는 채팅방이 있는지 확인
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByMember1AndMember2(member1, member2);
        if (existingRoom.isPresent()) {
            return ChatRoomResponse.from(existingRoom.get());
        }

        ChatRoom chatRoom = chatRoomRepository.save(request.toEntity(member1, member2));

        // ✅ 채팅방 생성 시 Redis 구독 추가
        redisSubscriptionManager.subscribeChatRoom(chatRoom.getId());

        return ChatRoomResponse.from(chatRoom);
    }
}
