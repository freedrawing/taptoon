package com.sparta.taptoon.domain.comment.controller;

import com.sparta.taptoon.domain.comment.dto.request.CommentRequest;
import com.sparta.taptoon.domain.comment.dto.response.CommentResponse;
import com.sparta.taptoon.domain.comment.service.CommentService;
import com.sparta.taptoon.domain.member.dto.MemberDetail;
import com.sparta.taptoon.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Comment", description = "댓글 API")
@AllArgsConstructor
@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 생성")
    @PostMapping("/matching-post/{matchingPostId}")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long matchingPostId,
            @Valid @RequestBody CommentRequest commentRequest
            ) {
        log.info("Received request: parentId={}, content={}, matchingPostId={}",
                commentRequest.parentId(), commentRequest.content(), matchingPostId);
        CommentResponse comment = commentService.makeComment(commentRequest, memberDetail.getMember(), matchingPostId);
        return ApiResponse.created(comment);
    }

    @Operation(summary = "댓글 수정")
    @PutMapping("/matching-post/{matchingPostId}/comment/{commentId}")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long matchingPostId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest commentRequest) {
        commentService.editComment(commentRequest,memberDetail.getMember(), matchingPostId, commentId);
        return ApiResponse.noContent();
    }

    @Operation(summary = "특정 포스트의 모든 댓글 조회 (대댓글 제외)")
    @GetMapping("/matching-post/{matchingPostId}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getAllCommentsFromMatchingPost(
            @PathVariable Long matchingPostId) {
        List<CommentResponse> commentsFromMatchingPost = commentService.findAllCommentsFromMatchingPost(matchingPostId);
        return ApiResponse.success(commentsFromMatchingPost);
    }

    @Operation(summary = "특정 댓글과 대댓글 조회")
    @GetMapping("/reply/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> getAllRepliesWithParentComment(
            @PathVariable Long commentId) {
        CommentResponse repliesWithParentComment = commentService.findAllRepliesWithParentComment(commentId);
        return ApiResponse.success(repliesWithParentComment);
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long commentId) {
        commentService.removeComment(memberDetail.getMember(), commentId);
        return ApiResponse.noContent();
    }
}
