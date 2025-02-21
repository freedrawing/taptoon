package com.sparta.taptoon.domain.comment.dto.response;

import com.sparta.taptoon.domain.comment.entity.Comment;
import lombok.Builder;

import java.time.LocalDateTime;
@Builder
public record CommentResponse(
        Long commentId,
        Long matchingPostId,
        Long memberId,
        String memberNickname,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    @Builder
    public static CommentResponse from(Comment comment) {
       return CommentResponse.builder()
                .commentId(comment.getId())
                .matchingPostId(comment.getMatchingPost().getId())
                .memberId(comment.getMember().getId())
                .memberNickname(comment.getMember().getNickname())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
