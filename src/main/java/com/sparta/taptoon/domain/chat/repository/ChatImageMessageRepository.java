package com.sparta.taptoon.domain.chat.repository;

import com.sparta.taptoon.domain.chat.entity.ChatImageMessage;
import com.sparta.taptoon.global.common.enums.Status;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatImageMessageRepository extends MongoRepository<ChatImageMessage, String> {

    List<ChatImageMessage> findByChatRoomIdOrderByCreatedAtAsc(String chatRoomId);


    // 스케줄러 관련
    List<ChatImageMessage> findByStatusAndCreatedAtBefore(Status status, LocalDateTime threshold);
    List<ChatImageMessage> findByStatus(Status status);
}
