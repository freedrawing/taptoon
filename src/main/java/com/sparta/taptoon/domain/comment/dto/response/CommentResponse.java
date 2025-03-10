package com.sparta.taptoon.domain.comment.dto.response;

import com.sparta.taptoon.domain.comment.entity.Comment;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record CommentResponse(
        Long commentId,
        Long matchingPostId,
        Long memberId,
        String memberName,
        Long parentId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentResponse> replies
) {
    // 공통적으로 사용하는 생성자(특정 댓글과 답글 조회 제외)
    public static CommentResponse from(Comment comment, List<CommentResponse> replies) {
        return new CommentResponse(
                comment.getId(),
                comment.getMatchingPost().getId(),
                comment.getMember().getId(),
                comment.getMember().getName(),
                getParentId(comment), // null checked
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                replies != null ? replies : Collections.emptyList() // 엔티티에 없기 때문에 responseDto에서 null check\
        );
    }
    // 생성자 하나로 합치면서 헬퍼메서드로 기존 매개변수 문제 해결
    public static CommentResponse from(Comment comment) {
        return from(comment, null);
    }

    // Optional ParentId null check logic
    private static Long getParentId(Comment comment) {
        return comment.getParent()
                .map(Comment::getId)
                .orElse(null);
    }
}
