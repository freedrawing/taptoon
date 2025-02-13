package com.sparta.taptoon.domain.chat.service;

import com.sparta.taptoon.domain.chat.dto.request.CreateChatRoomRequest;
import com.sparta.taptoon.domain.chat.dto.response.ChatRoomResponse;
import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.chat.repository.ChatRoomRepository;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    public ChatRoomResponse createChatRoom(CreateChatRoomRequest request) {
        Member member1 = memberRepository.findById(request.memberId1())
                .orElseThrow(() -> new RuntimeException("User not Found"));

        Member member2 = memberRepository.findById(request.memberId2())
                .orElseThrow(() -> new RuntimeException("User not Found"));

        // 기존에 같은 채팅방이 있는지 확인
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByMember1AndMember2(member1, member2);
        if (existingRoom.isPresent()) {
            return ChatRoomResponse.from(existingRoom.get());
        }

        // 채팅방 생성
        ChatRoom chatRoom = chatRoomRepository.save(request.toEntity(member1, member2));
        return ChatRoomResponse.from(chatRoom);
    }
}
