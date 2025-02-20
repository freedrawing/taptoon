package com.sparta.taptoon.domain.chat.service;

import com.sparta.taptoon.domain.chat.repository.ChatRoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;

    /**
     * 사용자가 특정 채팅방의 멤버인지 확인
     */
    @Transactional(readOnly = true)
    public boolean isMemberOfChatRoom(Long chatRoomId, Long memberId) {
        return chatRoomMemberRepository.existsByChatRoomIdAndMemberId(chatRoomId, memberId);
    }
}