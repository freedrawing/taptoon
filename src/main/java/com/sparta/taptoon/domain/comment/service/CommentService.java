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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final MatchingPostRepository matchingPostRepository;
    private final MemberRepository memberRepository;

    // 댓글 생성
    @Transactional
    public CommentResponse makeComment(CommentRequest commentRequest, Member member, Long matchingPostId) {
        // 매칭포스트 찾기
        MatchingPost foundMatchingPost = matchingPostRepository.findById(matchingPostId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATCHING_POST_NOT_FOUND));

        // 유저가 작성한 댓글
        Comment comment = commentRequest.toEntity(member, foundMatchingPost);

        // 유저가 작성한 댓글 저장
        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.from(savedComment);
    }

    // 답글(대댓글) 생성
    @Transactional
    public CommentResponse makeReply(CommentRequest commentRequest, Member member, Long commentId) {
        // 답글을 작성할 댓글을 찾기
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        // 답글에는 답글을 달 수 없음
        if (parentComment.getParent().isPresent()) {
            throw new InvalidRequestException(ErrorCode.INVALID_REQUEST);
        }

        // 댓글의 매칭포스트를 찾기
        MatchingPost matchingPostOfParentComment = parentComment.getMatchingPost();

        // 유저가 작성한 답글
        Comment comment = commentRequest.toEntity(member, parentComment, matchingPostOfParentComment);

        // 유저가 작성한 답글 저장
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

    // 특정 포스트의 모든 댓글 (답글 제외) 조회
    @Transactional(readOnly = true)
    public List<CommentResponse> findAllCommentsFromMatchingPost(Long matchingPostId) {
        // 특정 포스트에 달린 댓글 찾기
        List<Comment> foundComments = commentRepository.findAllByMatchingPostIdOrderByCreatedAt(matchingPostId);

        // 부모가 없는 댓글 Response에 담기
        List<CommentResponse> commentResponses = foundComments.stream()
                .filter(comment -> comment.getParent().isEmpty()) // parentId가 없는 댓글들 조회
                .map(CommentResponse::from)
                .collect(Collectors.toList());

        return commentResponses;
    }

    // 특정 댓글과 답글 조회
    @Transactional(readOnly = true)
    public CommentResponse findAllRepliesWithParentComment(Long commentId) {
        // 댓글 찾기
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        // 답글을 조회할 수 없음
        if (parentComment.getParent().isPresent()) {
            throw new InvalidRequestException(ErrorCode.INVALID_REQUEST);
        }

        // parentId로 답글 찾기
        List<Comment> replies = commentRepository.findAllByParentId(commentId);

        // 대댓글 ResponseDto에 담기
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

        // 댓글 삭제하고 답글들도 삭제하기
        List<Comment> allByParentId = commentRepository.findAllByParentId(commentId);
        allByParentId.forEach(Comment::remove);
    }
}
        /*
         1. isDeleted true인 댓글들 조회 안되게 예외처리
         2. 댓글은 자식까지만 (손주, 증손주 허용안됨)
         3. 특정 댓글과 대댓글 조회에서 답글은 조회대상에서 제외시키기
         4. 포스트에서 댓글만 조회할때 답글이 있는지 없는지 response에서 알려주기
         5. 조회 api Pageable통해 페이징처리 기능 추가
         6. 이모티콘 기능 추가
         7. 사용자 닉네임 태그 기능
         */