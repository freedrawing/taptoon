package com.sparta.taptoon.domain.chat.repository;

import com.sparta.taptoon.domain.chat.entity.ChatImageMessage;
import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatImageMessageRepository extends JpaRepository<ChatImageMessage, Long> {

    Optional<ChatImageMessage> findByIdAndChatRoom(Long id, ChatRoom chatRoom);
    List<ChatImageMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);
    List<ChatImageMessage> findByChatRoomAndIdGreaterThan(ChatRoom chatRoom, Long id);
}
