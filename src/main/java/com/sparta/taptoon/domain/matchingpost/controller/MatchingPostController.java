package com.sparta.taptoon.domain.matchingpost.controller;

import com.sparta.taptoon.domain.matchingpost.dto.request.AddMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.dto.request.UpdateMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostResponse;
import com.sparta.taptoon.domain.matchingpost.service.MatchingPostService;
import com.sparta.taptoon.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MatchingPostController {

    private final MatchingPostService matchingPostService;

    // 매칭 포스트 등록
    @PostMapping("/matching-posts")
    public ResponseEntity<ApiResponse<MatchingPostResponse>> createMatchingPost(@Valid @RequestBody AddMatchingPostRequest request) {
        MatchingPostResponse response = matchingPostService.makeNewMatchingPost(1L, request);
        return ApiResponse.created(response);
    }

    // 매칭 포스트 단건 조회
    @GetMapping("/matching-posts/{matchingPostId}")
    public ResponseEntity<ApiResponse<MatchingPostResponse>> getMatchingPost(@PathVariable Long matchingPostId) {
        MatchingPostResponse response = matchingPostService.findMatchingPostAndUpdateViewsV3(matchingPostId);
//        MatchingPostResponse response = matchingPostService.findMatchingPostWithRedisson(matchingPostId);
        return ApiResponse.success(response);
    }

    // 매칭 포스트 수정 (일괄 수정)
    @PutMapping("/matching-posts/{matchingPostId}")
    public ResponseEntity<ApiResponse<MatchingPostResponse>> updateMatchingPost(
            @PathVariable Long matchingPostId,
            @Valid @RequestBody UpdateMatchingPostRequest request) {

        MatchingPostResponse response = matchingPostService.modifyMatchingPost(1L, matchingPostId, request);
        return ApiResponse.success(response);
    }

    // 매칭 포스트 삭제 (soft deletion)
    @DeleteMapping("/matching-posts/{matchingPostId}")
    public ResponseEntity<ApiResponse<Void>> deleteMatchingPost(@PathVariable Long matchingPostId) {
        matchingPostService.removeMatchingPost(1L, matchingPostId);
        return ApiResponse.noContent();
    }

    // 매칭 포스트 다건 조회 (검색)
    @GetMapping("/matching-posts")
    public ResponseEntity<ApiResponse<Page<MatchingPostResponse>>> getFilteredMatchingPosts(
            @RequestParam(required = false) String artistType,
            @RequestParam(required = false) String workType,
            @RequestParam(required = false) String keyword
    ) {

        return null;
    }

    // 이미지랑 텍스트 업데이트 하는 API 추가로 만들어야 함
    @GetMapping("/matching-posts/file")
    public ResponseEntity<ApiResponse<Void>> uploadFile() {
        return null;
    }
}
