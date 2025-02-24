package com.sparta.taptoon.domain.comment.dto.response;

import com.sparta.taptoon.domain.comment.entity.Comment;
import lombok.Builder;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CommentResponse(
        Long commentId,
        Long matchingPostId,
        Long memberId,
        String memberNickname,

        @Nullable
        Long parentId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentResponse> replies
) {
    // 특정 포스트의 모든 댓글 (대댓글 제외) 조회
    @Builder
    public static CommentResponse from(Comment comment) {
       return CommentResponse.builder()
                .commentId(comment.getId())
                .matchingPostId(comment.getMatchingPost().getId())
                .memberId(comment.getMember().getId())
                .memberNickname(comment.getMember().getNickname())
                .parentId(comment.getParent().getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
    // 특정 댓글과 대댓글 조회
    @Builder
    public static CommentResponse from(Comment comment, List<CommentResponse> replies) {
        return CommentResponse.builder()
                .commentId(comment.getId())
                .matchingPostId(comment.getMatchingPost().getId())
                .memberId(comment.getMember().getId())
                .memberNickname(comment.getMember().getNickname())
                .parentId(comment.getParent().getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(replies)
                .build();
    }
}
