package com.sparta.taptoon.domain.matchingpost.controller;

import com.sparta.taptoon.domain.matchingpost.dto.request.AddMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.dto.request.UpdateMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostResponse;
import com.sparta.taptoon.domain.matchingpost.service.MatchingPostService;
import com.sparta.taptoon.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "matching-posts", description = "매칭보드 게시글 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/matching-posts")
public class MatchingPostController {

    private final MatchingPostService matchingPostService;

    @Operation(summary = "매칭보드에 게시글 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<MatchingPostResponse>> createMatchingPost(@Valid @RequestBody AddMatchingPostRequest request) {
        MatchingPostResponse response = matchingPostService.makeNewMatchingPost(1L, request);
        return ApiResponse.created(response);
    }

    @Operation(summary = "매칭 게시글 단건 조회")
    @GetMapping("/{matchingPostId}")
    public ResponseEntity<ApiResponse<MatchingPostResponse>> getMatchingPost(@PathVariable Long matchingPostId) {
        MatchingPostResponse response = matchingPostService.findMatchingPostAndUpdateViewsV3(matchingPostId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "매칭 게시글 수정 (일괄 수정)")
    @PutMapping("/{matchingPostId}")
    public ResponseEntity<ApiResponse<MatchingPostResponse>> updateMatchingPost(
            @PathVariable Long matchingPostId,
            @Valid @RequestBody UpdateMatchingPostRequest request) {

        MatchingPostResponse response = matchingPostService.modifyMatchingPost(1L, matchingPostId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "매칭 게시글 삭제 (soft deletion)")
    @DeleteMapping("/{matchingPostId}")
    public ResponseEntity<ApiResponse<Void>> deleteMatchingPost(@PathVariable Long matchingPostId) {
        matchingPostService.removeMatchingPost(1L, matchingPostId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "매칭 게시글 다건 조회 (검색)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MatchingPostResponse>>> getFilteredMatchingPosts(
            @RequestParam(required = false) String artistType,
            @RequestParam(required = false) String workType,
            @RequestParam(required = false) String keyword,
            @PageableDefault Pageable pageable
    ) {
        Page<MatchingPostResponse> response = matchingPostService.findFilteredMatchingPosts(artistType, workType, keyword, pageable);
        return ApiResponse.success(response);
    }

}
