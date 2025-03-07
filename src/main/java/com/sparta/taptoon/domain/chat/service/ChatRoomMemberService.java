package com.sparta.taptoon.domain.chat.service;

import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.chat.entity.ChatRoomMember;
import com.sparta.taptoon.domain.chat.repository.ChatRoomMemberRepository;
import com.sparta.taptoon.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 사용자가 특정 채팅방의 멤버인지 확인
     */
    @Transactional(readOnly = true)
    public boolean isMemberOfChatRoom(String chatRoomId, Long memberId) {
        return chatRoomMemberRepository.existsByChatRoomIdAndMemberId(chatRoomId, memberId);
    }

    @Transactional(readOnly = true)
    public List<Long> getChatRoomMembers(String chatRoomId) {
        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomId(chatRoomId);
        return members.stream()
                .map(ChatRoomMember::getMemberId)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> getChatRoomsForUser(Long userId) {
        List<ChatRoomMember> memberEntries = chatRoomMemberRepository.findByMemberId(userId);
        List<String> chatRoomIds = memberEntries.stream()
                .map(ChatRoomMember::getChatRoomId)
                .collect(Collectors.toList());
        return chatRoomRepository.findAllById(chatRoomIds);
    }
}