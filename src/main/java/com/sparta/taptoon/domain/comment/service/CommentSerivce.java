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
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
import com.sparta.taptoon.global.error.exception.InvalidRequestException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    // 댓글 수정
    @Transactional
    public void editComment(
            CommentRequest commentRequest,
            Member member,
            Long matchingPostId,
            Long commentId) {
        // 수정할 댓글 찾기
        Comment foundComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));
        // 수정할 댓글이 멤버가 작성한 댓글인지 검증
        if (!foundComment.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException(ErrorCode.COMMENT_ACCESS_DENIED);
        }
        // 수정할 댓글이 포스트의 댓글인지 검증
        if (!foundComment.getMatchingPost().getId().equals(matchingPostId)) {
            throw new InvalidRequestException(ErrorCode.INVALID_REQUEST);
        }
        foundComment.updateComment(commentRequest);
    }

    // 특정 포스트의 모든 댓글 조회
    public List<CommentResponse> findAllCommentsFromMatchingPost(Long matchingPostId) {
        // 특정 포스트에 달린 댓글 찾기
        List<Comment> foundComments = commentRepository.findAllByMatchingPostId(matchingPostId);
        // 댓글 Response에 담기
        List<CommentResponse> commentResponses = foundComments.stream()
                .map(comment -> CommentResponse.from(comment))
                .collect(Collectors.toList());

        return commentResponses;
    }
}
