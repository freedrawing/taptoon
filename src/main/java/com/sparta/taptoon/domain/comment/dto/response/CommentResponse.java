package com.sparta.taptoon.domain.comment.dto.response;

import com.sparta.taptoon.domain.comment.entity.Comment;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CommentResponse(
        Long commentId,
        Long matchingPostId,
        Long memberId,
        String memberNickname,
        Long parentId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentResponse> replies
) {
    // 공통적으로 사용하는 생성자(특정 댓글과 답글 조회 제외)
    @Builder
    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .commentId(comment.getId())
                .matchingPostId(comment.getMatchingPost().getId())
                .memberId(comment.getMember().getId())
                .memberNickname(comment.getMember().getNickname())
                .parentId(getParentId(comment)) // null checked
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
    // 특정 댓글과 답글 조회시에만 적용
    @Builder
    public static CommentResponse from(Comment comment, List<CommentResponse> replies) {
        return CommentResponse.builder()
                .commentId(comment.getId())
                .matchingPostId(comment.getMatchingPost().getId())
                .memberId(comment.getMember().getId())
                .memberNickname(comment.getMember().getNickname())
                .parentId(getParentId(comment)) // null checked
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(replies)
                .build();
    }

    // Optional ParentId null check logic
    private static Long getParentId(Comment comment) {
        return comment.getParent()
                .map(Comment::getId)
                .orElse(null);
    }
}
