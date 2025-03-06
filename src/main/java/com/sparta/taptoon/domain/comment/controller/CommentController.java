package com.sparta.taptoon.domain.comment.controller;

import com.sparta.taptoon.domain.comment.dto.request.CommentRequest;
import com.sparta.taptoon.domain.comment.dto.response.CommentResponse;
import com.sparta.taptoon.domain.comment.service.CommentService;
import com.sparta.taptoon.domain.member.dto.MemberDetail;
import com.sparta.taptoon.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Comment", description = "댓글 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @RequestParam Long matchingPostId,
            @Valid @RequestBody CommentRequest commentRequest) {
        CommentResponse comment = commentService.makeComment(commentRequest, memberDetail.getMember(), matchingPostId);
        return ApiResponse.created(comment);
    }

    @Operation(summary = "답글 생성")
    @PostMapping("/{parentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> createReply(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long parentId,
            @Valid @RequestBody CommentRequest commentRequest) {
        CommentResponse reply = commentService.makeReply(commentRequest, memberDetail.getMember(),parentId);
        return ApiResponse.created(reply);
    }

    @Operation(summary = "댓글과 답글 수정")
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @RequestParam Long matchingPostId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest commentRequest) {
        commentService.editComment(commentRequest,memberDetail.getMember(), matchingPostId, commentId);
        return ApiResponse.noContent();
    }

    @Operation(summary = "특정 포스트의 모든 댓글 조회 (답글 제외)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getAllCommentsFromMatchingPost(
            @RequestParam Long matchingPostId) {
        List<CommentResponse> commentsFromMatchingPost = commentService.findAllCommentsFromMatchingPost(matchingPostId);
        return ApiResponse.success(commentsFromMatchingPost);
    }

    @Operation(summary = "특정 댓글과 답글 조회")
    @GetMapping("/replies/{parentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> getAllRepliesWithParentComment(
            @PathVariable Long parentId) {
        CommentResponse repliesWithParentComment = commentService.findAllRepliesWithParentComment(parentId);
        return ApiResponse.success(repliesWithParentComment);
    }

    @Operation(summary = "댓글과 답글 삭제")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long commentId) {
        commentService.removeComment(memberDetail.getMember(), commentId);
        return ApiResponse.noContent();
    }
}
