package com.sparta.taptoon.domain.chat.repository;

import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findByMembers_MemberIdAndIsDeletedFalse(Long memberId);

    @Query("SELECT c.id FROM ChatRoom c WHERE c.isDeleted = false ")
    List<Long> findChatRoomIds();

    @Query("SELECT cr FROM ChatRoom cr JOIN cr.members crm " +
            "WHERE cr.isDeleted = false " +
            "GROUP BY cr " +
            "HAVING COUNT(crm.member) = :memberCount " +
            "AND SUM(CASE WHEN crm.member.id IN :memberIds THEN 1 ELSE 0 END) = :memberCount " +
            "AND SUM(CASE WHEN crm.member.id NOT IN :memberIds THEN 1 ELSE 0 END) = 0")
    Optional<ChatRoom> findByExactMembers(@Param("memberIds") List<Long> memberIds, @Param("memberCount") int memberCount);
}
