package com.sparta.taptoon.domain.chat.repository;

import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.chat.entity.ChatRoomMember;
import com.sparta.taptoon.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    /** 특정 사용자가 해당 채팅방의 멤버인지 여부 확인 */
    boolean existsByChatRoomIdAndMember_Id(Long chatRoomId, Long memberId);

    /** 특정 채팅방에 해당 멤버가 존재하는지 여부 체크 */
    boolean existsByChatRoomAndMember(ChatRoom chatRoom, Member member);

    List<ChatRoomMember> findByChatRoomId(Long chatRoomId);

    List<ChatRoomMember> findByMember_Id(Long memberId);
}
