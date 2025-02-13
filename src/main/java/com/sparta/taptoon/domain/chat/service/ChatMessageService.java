package com.sparta.taptoon.domain.chat.service;

import com.sparta.taptoon.domain.chat.dto.request.SendChatMessageRequest;
import com.sparta.taptoon.domain.chat.dto.response.ChatMessageResponse;
import com.sparta.taptoon.domain.chat.entity.ChatMessage;
import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.chat.repository.ChatMessageRepository;
import com.sparta.taptoon.domain.chat.repository.ChatRoomRepository;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    public ChatMessageResponse sendMessage(SendChatMessageRequest request){
        ChatRoom chatRoom = chatRoomRepository.findById(request.chatRoomId())
                .orElseThrow(()-> new IllegalArgumentException("채팅방을 찾을 수 없습니다"));
        Member sender = memberRepository.findById(request.senderId())
                .orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        ChatMessage chatMessage = request.toEntity(chatRoom, sender);
        chatMessageRepository.save(chatMessage);

        return ChatMessageResponse.from(chatMessage);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessagesByChatRoom(Long chatRoomId) {
        // chatRoomId를 기반으로 ChatRoom 객체를 먼저 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        // chatRoom 객체를 이용하여 메시지 조회
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);

        return messages.stream().map(ChatMessageResponse::from).collect(Collectors.toList());
    }

}
