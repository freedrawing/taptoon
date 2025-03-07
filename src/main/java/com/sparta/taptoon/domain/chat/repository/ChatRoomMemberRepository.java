package com.sparta.taptoon.domain.chat.repository;

import com.sparta.taptoon.domain.chat.entity.ChatRoomMember;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomMemberRepository extends MongoRepository<ChatRoomMember, String> {

    /** 특정 사용자가 해당 채팅방의 멤버인지 여부 확인 */
    boolean existsByChatRoomIdAndMemberId(String chatRoomId, Long memberId);

    List<ChatRoomMember> findByChatRoomId(String chatRoomId);

    List<ChatRoomMember> findByMemberId(Long memberId);
}
