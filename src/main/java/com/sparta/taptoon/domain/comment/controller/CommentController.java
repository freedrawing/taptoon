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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comment", description = "댓글 API")
@AllArgsConstructor
@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @Valid @RequestBody CommentRequest commentRequest, Long matchingPostId) {
        CommentResponse comment = commentService.makeComment(commentRequest, memberDetail.getMember(), matchingPostId);
        return ApiResponse.created(comment);
    }

    @Operation(summary = "댓글 수정")
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @Valid @PathVariable Long commentId,
            @RequestBody CommentRequest commentRequest, Long matchingPostId) {
        commentService.editComment(commentRequest,memberDetail.getMember(), matchingPostId, commentId);
        return ApiResponse.noContent();
    }

    @Operation(summary = "특정 포스트의 모든 댓글 조회")
    @GetMapping("/{matchingPostId}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getAllCommentsFromMatchingPost(
            @PathVariable Long matchingPostId) {
        List<CommentResponse> commentsFromMatchingPost = commentService.findAllCommentsFromMatchingPost(matchingPostId);
        return ApiResponse.success(commentsFromMatchingPost);
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
