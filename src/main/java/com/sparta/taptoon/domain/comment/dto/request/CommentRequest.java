package com.sparta.taptoon.domain.comment.dto.request;

import com.sparta.taptoon.domain.comment.entity.Comment;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.member.entity.Member;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CommentRequest(
        // parentId = 부모 : commentId = 1인 댓글에 대댓글을 달면 해당 대댓글의 parentId는 1이 된다.
        Long parentId,

        @NotBlank(message = "내용을 입력해주세요.")
        @Size(max = 100, message = "100자 이내로 입력해주세요.")
        String content

        ) {
    public Comment toEntity(Member member, MatchingPost matchingPost, Comment parent) {
        return Comment.builder()
                .member(member)
                .matchingPost(matchingPost)
                .parent(parent)
                .content(content)
                .build();
    }
}