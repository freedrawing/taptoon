package com.sparta.taptoon.domain.chat.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter
@Document(collection = "chat_room")
public class ChatRoom {

    @Id
    private String id;

    @Field(name = "member_ids")
    @Indexed // memberIds로 빠른 조회를 위해 인덱스 추가
    private List<Long> memberIds;

    @Field(name = "is_deleted")
    private boolean isDeleted;

    @Builder
    public ChatRoom(List<Long> memberIds) {
        this.memberIds = memberIds;
        this.isDeleted = false;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void addMember(Long memberId) {
        if (!this.memberIds.contains(memberId)) {
            this.memberIds.add(memberId);
        }
    }

    public void removeMember(Long memberId) {
        this.memberIds.remove(memberId);
    }
}
