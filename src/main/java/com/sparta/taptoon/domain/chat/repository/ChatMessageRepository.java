package com.sparta.taptoon.domain.chat.repository;

import com.sparta.taptoon.domain.chat.entity.ChatMessage;
import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

    /** 특정 메시지 ID 이후의 안 읽은 메시지 조회 */
    List<ChatMessage> findByChatRoomAndIdGreaterThan(ChatRoom chatRoom, Long lastReadMessageId);

    /** 채팅방 마지막 메시지 조회 */
    Optional<ChatMessage> findTopByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    /** 마지막으로 읽은 메시지 이후 메시지 개수 조회 */
    int countByChatRoomAndIdGreaterThan(ChatRoom chatRoom, Long lastReadMessageId);

}
