package com.sparta.taptoon.domain.chat.repository;

import com.sparta.taptoon.domain.chat.entity.ChatImageMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ChatImageMessageRepository extends MongoRepository<ChatImageMessage, String> {

    Optional<ChatImageMessage> findByIdAndChatRoomId(String id, String chatRoomId);

    List<ChatImageMessage> findByChatRoomIdOrderByCreatedAtAsc(String chatRoomId);

    List<ChatImageMessage> findByChatRoomIdAndIdGreaterThan(String chatRoomId, String id);
}
