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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member1_id", nullable = false)
    private Member member1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member2_id", nullable = false)
    private Member member2;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    public ChatRoom(Member member1, Member member2, Boolean isDeleted) {
        this.member1 = member1;
        this.member2 = member2;
        this.isDeleted = isDeleted;
    }
}
