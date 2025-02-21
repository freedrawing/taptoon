package com.sparta.taptoon.domain.comment.controller;

import com.sparta.taptoon.domain.comment.dto.request.CommentRequest;
import com.sparta.taptoon.domain.comment.dto.response.CommentResponse;
import com.sparta.taptoon.domain.comment.service.CommentSerivce;
import com.sparta.taptoon.domain.member.dto.MemberDetail;
import com.sparta.taptoon.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Comment", description = "댓글 API")
@AllArgsConstructor
@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentSerivce commentSerivce;

    @Operation(summary = "댓글 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @Valid @RequestBody CommentRequest commentRequest, Long matchingPostId) {
        CommentResponse comment = commentSerivce.makeComment(commentRequest, memberDetail.getMember(), matchingPostId);
        return ApiResponse.created(comment);
    }

}
