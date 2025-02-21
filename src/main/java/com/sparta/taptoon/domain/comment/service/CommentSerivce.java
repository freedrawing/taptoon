package com.sparta.taptoon.domain.comment.service;

import com.sparta.taptoon.domain.comment.dto.request.CommentRequest;
import com.sparta.taptoon.domain.comment.dto.response.CommentResponse;
import com.sparta.taptoon.domain.comment.entity.Comment;
import com.sparta.taptoon.domain.comment.repository.CommentRepository;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostRepository;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CommentSerivce {

    private final CommentRepository commentRepository;
    private final MatchingPostRepository matchingPostRepository;
    private final MemberRepository memberRepository;

    // 댓글 생성
    @Transactional
    public CommentResponse makeComment(CommentRequest commentRequest, Member member, Long matchingPostId) {
        // 유저 찾기
        Member foundMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        // 매칭포스트 찾기
        MatchingPost foundMatchingPost = matchingPostRepository.findById(matchingPostId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATCHING_POST_NOT_FOUND));

        // 유저가 작성한 댓글
        Comment comment = commentRequest.toEntity(foundMember, foundMatchingPost);
        // 유저가 작성한 댓글 저장
        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.from(savedComment);
    }
}
