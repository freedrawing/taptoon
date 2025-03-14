package com.sparta.taptoon.domain.comment.entity;

import com.sparta.taptoon.domain.comment.dto.request.CommentRequest;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.util.Optional;

@Where(clause = "is_deleted = false") // 삭제된 댓글 조회에서 제외
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

    @Column(name = "content", nullable = false, length = 3000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", updatable = false)
    private Comment parent;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Builder
    public Comment(
            Member member,
            MatchingPost matchingPost,
            Comment parent,
            String content
    ) {
        this.member = member;
        this.matchingPost = matchingPost;
        this.parent = parent;
        this.content = content;
        this.isDeleted = false;
    }

    // parentId null check
    public Optional<Comment> getParent() {
        return Optional.ofNullable(parent);
    }

    // 댓글 수정 메서드
    public void updateComment(CommentRequest commentRequest) {
        this.content = commentRequest.content();
    }

    // 댓글 Soft Delete 메서드
    public void remove() {
        this.isDeleted = true;
    }
}
