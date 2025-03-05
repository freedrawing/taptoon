package com.sparta.taptoon.domain.chat.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@NoArgsConstructor
@Document(collection = "chat_room_member")
public class ChatRoomMember {

    @Id
    private String id;

    @Field(name = "chat_room_id")
    private String chatRoomId;

    @Field(name = "member_id")
    private Long memberId;

    @Builder
    public ChatRoomMember(String chatRoomId, Long memberId) {
        this.chatRoomId = chatRoomId;
        this.memberId = memberId;
    }

    public Long getMemberId() {
        return memberId;
    }
}