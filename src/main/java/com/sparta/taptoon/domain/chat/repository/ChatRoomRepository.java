package com.sparta.taptoon.domain.chat.repository;

import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    // 특정 멤버가 포함된 채팅방 목록 조회
    List<ChatRoom> findByMemberIdsContainsAndIsDeletedFalse(Long memberId);

    // Projection으로 _id만 조회
    @Query(value = "{ 'isDeleted': false }", fields = "{ '_id': 1 }")
    List<IdProjection> findChatRoomIds();

    // 멤버 목록으로 채팅방 조회
    @Query("{ 'isDeleted': false, 'memberIds': { $size: ?1, $all: ?0 } }")
    Optional<ChatRoom> findByExactMembers(List<Long> memberIds, int memberCount);

    // chatRoomId와 memberId로 멤버십 확인
    @Query(value = "{ '_id': ?0, 'memberIds': { $in: [?1] } }", exists = true)
    boolean existsByIdAndMemberId(String chatRoomId, Long memberId);

    // Projection 인터페이스
    interface IdProjection {
        String getId();
    }
}
