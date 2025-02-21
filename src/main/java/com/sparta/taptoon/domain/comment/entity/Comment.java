package com.sparta.taptoon.domain.comment.entity;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "comment")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_post_id", nullable = false, updatable = false)
    private MatchingPost matchingPost;

    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Builder
    public Comment(Member member, MatchingPost matchingPost, Comment parent, String content, boolean isDeleted) {
        this.member = member;
        this.matchingPost = matchingPost;
        this.parent = parent;
        this.content = content;
        this.isDeleted = isDeleted;
    }
}
