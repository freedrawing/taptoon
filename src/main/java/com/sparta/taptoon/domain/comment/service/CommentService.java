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
public class CommentService {

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

        // 댓글은 부모 객체를 가지지 않는 것이 default
        Comment parent = null;
        // 만약 requestDto에서 parentId를 입력받았다면 그 댓글의 parent를 찾기 (대댓글로 결정되는 과정)
        if (commentRequest.parentId() != null) {
            parent = commentRepository.findById(commentRequest.parentId())
                    .orElseThrow(()-> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));
        }
        // 유저가 작성한 댓글
        Comment comment = commentRequest.toEntity(foundMember, foundMatchingPost, parent);

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
        // 만약 requestDto에서 parentId를 입력받았다면 그 댓글의 parent를 찾기
        if (commentRequest.parentId() != null) {
            commentRepository.findById(commentRequest.parentId())
                    .orElseThrow(()-> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));
        }

        foundComment.updateComment(commentRequest);
    }

    // 특정 포스트의 모든 댓글 (대댓글 제외) 조회
    public List<CommentResponse> findAllCommentsFromMatchingPost(Long matchingPostId) {
        // 특정 포스트에 달린 댓글 찾기
        List<Comment> foundComments = commentRepository.findAllByMatchingPostIdOrderByCreatedAt(matchingPostId);

        // 부모가 없는 댓글 Response에 담기
        List<CommentResponse> commentResponses = foundComments.stream()
                .filter(comment -> comment.getParent() == null) // parentId가 null인 댓글들 조회
                .map(CommentResponse::from)
                .collect(Collectors.toList());

        return commentResponses;
    }

    // 특정 댓글과 대댓글 조회
    public CommentResponse findAllRepliesWithParentComment(Long commentId) {
        // 댓글 찾기
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));
        // parentId로 대댓글 찾기
        List<Comment> replies = commentRepository.findAllById(commentId);
        // 대댓글이 부모 댓글의 id를 제대로 갖고있는지 비교 검증
        for (Comment reply : replies) {
            if (!reply.getParent().getId().equals(parentComment.getId())) {
                throw new InvalidRequestException(ErrorCode.INVALID_REQUEST);
            }
        }
        // id 비교 검증 로직으로 모두 통과되면 대댓글 ResponseDto에 담기
        List<CommentResponse> repliesResponses = replies.stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());

        return CommentResponse.from(parentComment, repliesResponses);
    }

    // 댓글 삭제
    @Transactional
    public void removeComment(Member member, Long commentId) {
        // 삭제할 댓글 찾기
        Comment foundComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        // 삭제할 댓글이 유저가 작성한 댓글이 맞는지 검증
        if (!foundComment.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException(ErrorCode.COMMENT_ACCESS_DENIED);
        }
        // 댓글 Soft Delete
        foundComment.remove();

        /*
         1. 댓글 isDeleted 할때 대댓글이 있으면 대댓글도 함께 지울것인지?
         2. 이 isDeleted가 이미 true인 댓글 중복 삭제 요청 안되게 하기
         3. isDeleted true인 댓글들 조회 안되게 예외처리
         4.
         */

    }
}
