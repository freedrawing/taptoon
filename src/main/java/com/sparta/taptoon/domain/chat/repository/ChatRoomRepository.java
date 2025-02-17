package com.sparta.taptoon.domain.chat.repository;

import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByMember1AndMember2(Member member1, Member member2);

    @Query("SELECT c.id FROM ChatRoom c WHERE c.isDeleted = false ")
    List<Long> findChatRoomIds();
}
