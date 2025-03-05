package com.sparta.taptoon.domain.chat.repository;

import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    List<ChatRoom> findByMemberIdsContainsAndIsDeletedFalse(Long memberId);

    @Query(value = "{ 'isDeleted': false }", fields = "{ '_id': 1 }")
    List<String> findChatRoomIds();

    @Query("{ 'isDeleted': false, 'memberIds': { $size: ?1, $all: ?0 } }")
    Optional<ChatRoom> findByExactMembers(List<Long> memberIds, int memberCount);
}
