package com.sparta.taptoon.domain.chat.service;

import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.chat.entity.ChatRoomMember;
import com.sparta.taptoon.domain.chat.repository.ChatRoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;

    /**
     * 사용자가 특정 채팅방의 멤버인지 확인
     */
    @Transactional(readOnly = true)
    public boolean isMemberOfChatRoom(Long chatRoomId, Long memberId) {
        return chatRoomMemberRepository.existsByChatRoomIdAndMember_Id(chatRoomId, memberId);
    }

    @Transactional(readOnly = true)
    public List<Long> getChatRoomMembers(Long chatRoomId) {
        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomId(chatRoomId);
        return members.stream()
                .map(ChatRoomMember::getMemberId)
                .collect(Collectors.toList());
    }

    public List<ChatRoom> getChatRoomsForUser(Long userId) {
        return chatRoomMemberRepository.findByMember_Id(userId)
                .stream()
                .map(ChatRoomMember::getChatRoom)
                .collect(Collectors.toList());
    }
}