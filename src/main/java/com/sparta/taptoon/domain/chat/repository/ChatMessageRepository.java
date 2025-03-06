package com.sparta.taptoon.domain.chat.repository;

import com.sparta.taptoon.domain.chat.entity.ChatMessage;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    // 채팅방의 메시지를 생성 시간 기준 오름차순으로 조회
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(String chatRoomId);

    // 특정 메시지 ID 이후의 메시지 조회
    List<ChatMessage> findByChatRoomIdAndIdGreaterThan(String chatRoomId, ObjectId lastReadMessageId);

    // 채팅방의 마지막 메시지 조회
    Optional<ChatMessage> findTopByChatRoomIdOrderByCreatedAtDesc(String chatRoomId);

    // 마지막으로 읽은 메시지 이후 메시지 개수 조회
    int countByChatRoomIdAndIdGreaterThan(String chatRoomId, ObjectId lastReadMessageId);

    // 채팅방의 전체 메시지 수 조회
    int countByChatRoomId(String chatRoomId);

    // 마지막으로 읽은 메시지 이후, 발신자를 제외한 안 읽은 메시지 개수 조회
    @Query(value = "{ 'chatRoomId': ?0, '_id': { $gt: ?1 }, 'senderId': { $ne: ?2 } }", count = true)
    int countUnreadMessagesExcludingSender(String chatRoomId, ObjectId lastReadMessageId, Long memberId);
}