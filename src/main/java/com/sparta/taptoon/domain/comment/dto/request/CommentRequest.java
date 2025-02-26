package com.sparta.taptoon.domain.comment.dto.request;

import com.sparta.taptoon.domain.comment.entity.Comment;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.member.entity.Member;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;


@Builder
public record CommentRequest(

        @NotBlank(message = "내용을 입력해주세요.") // null 방지
        @Size(max = 100, message = "100자 이내로 입력해주세요.")
        String content) {

    // 댓글 생성, 수정 & 답글 수정
    @Builder
    public Comment toEntity(Member member, MatchingPost matchingPost) {
        return Comment.builder()
                .member(member)
                .matchingPost(matchingPost)
                .content(content)
                .build();
    }

    // 답글 생성 전용 생성자
    @Builder
    public Comment toEntity(Member member, Comment parent, MatchingPost matchingPost) {
        return Comment.builder()
                .member(member)
                .parent(parent)
                .matchingPost(matchingPost)
                .content(content)
                .build();
    }
}