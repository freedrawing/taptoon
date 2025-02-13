package com.sparta.taptoon.domain.chat.entity;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "chat_room")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private Member member1Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private Member member2Id;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Builder
    public ChatRoom(String name, Member member1Id, Member member2Id, Boolean isDeleted) {
        this.name = name;
        this.member1Id = member1Id;
        this.member2Id = member2Id;
        this.isDeleted = isDeleted;
    }
}
