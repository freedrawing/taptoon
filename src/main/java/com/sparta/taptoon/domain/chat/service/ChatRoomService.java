package com.sparta.taptoon.domain.chat.service;

import com.sparta.taptoon.domain.chat.dto.request.CreateChatRoomRequest;
import com.sparta.taptoon.domain.chat.dto.response.ChatRoomResponse;
import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.chat.repository.ChatRoomRepository;
import com.sparta.taptoon.domain.user.entity.User;
import com.sparta.taptoon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public ChatRoomResponse createChatRoom(CreateChatRoomRequest request) {
        User user1 = userRepository.findById(request.userId1())
                .orElseThrow(() -> new RuntimeException("User not Found"));

        User user2 = userRepository.findById(request.userId2())
                .orElseThrow(() -> new RuntimeException("User not Found"));

        // 기존에 같은 채팅방이 있는지 확인
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByUser1AndUser2(user1, user2);
        if (existingRoom.isPresent()) {
            return ChatRoomResponse.from(existingRoom.get());
        }

        // 채팅방 생성
        ChatRoom chatRoom = chatRoomRepository.save(request.toEntity(user1, user2));
        return ChatRoomResponse.from(chatRoom);
    }
}
