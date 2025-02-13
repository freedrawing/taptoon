package com.sparta.taptoon.domain.chat.dto.request;

import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.member.entity.Member;

public record CreateChatRoomRequest(Long memberId1, Long memberId2) {
    public ChatRoom toEntity(Member member1, Member member2) {
        return ChatRoom.builder()
                .member1(member1)
                .member2(member2)
                .isDeleted(false)
                .build();
    }
}
