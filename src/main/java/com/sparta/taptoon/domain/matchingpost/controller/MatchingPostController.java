package com.sparta.taptoon.domain.matchingpost.controller;

import com.sparta.taptoon.domain.matchingpost.dto.request.RegisterMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.dto.request.UpdateMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostCursorResponse;
import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostResponse;
import com.sparta.taptoon.domain.matchingpost.service.ElasticAutocompleteService;
import com.sparta.taptoon.domain.matchingpost.service.MatchingPostService;
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
@Tag(name = "matching-posts", description = "매칭보드 게시글 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matching-posts")
public class MatchingPostController {

    private final MatchingPostService matchingPostService;
    private final ElasticAutocompleteService elasticAutocompleteService;

    @Operation(summary = "MatchingPost 글쓰기 버튼 클릭")
    @PostMapping("/write")
    public ResponseEntity<ApiResponse<Long>> createSkeleton(
            @AuthenticationPrincipal MemberDetail memberDetail
    ) {
        Long id = matchingPostService.generateEmptyMatchingPost(memberDetail.getMember());
        return ApiResponse.success(id);
    }

    @Operation(summary = "매칭 게시글 등록")
    @PutMapping("/{matchingPostId}/registration")
    public ResponseEntity<ApiResponse<MatchingPostResponse>> registerMatchingPost(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long matchingPostId,
            @Valid @RequestBody RegisterMatchingPostRequest request) {

        MatchingPostResponse response = matchingPostService.registerMatchingPost(memberDetail.getMember(), matchingPostId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "매칭 게시글 수정")
    @PutMapping("/{matchingPostId}")
    public ResponseEntity<ApiResponse<Void>> updateMatchingPost(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long matchingPostId,
            @Valid @RequestBody UpdateMatchingPostRequest request
    ) {
        matchingPostService.editMatchingPost(memberDetail.getId(), matchingPostId, request);
        return ApiResponse.noContent();
    }

    @Operation(summary = "매칭 게시글 단건 조회 (deprecated, 진짜 등록할 때는 `PUT`으로 함)")
    @GetMapping("/{matchingPostId}")
    public ResponseEntity<ApiResponse<MatchingPostResponse>> getMatchingPost(@PathVariable Long matchingPostId) {
        MatchingPostResponse response = matchingPostService.findMatchingPostAndUpdateViews(matchingPostId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "매칭 게시글 삭제 (soft deletion)")
    @DeleteMapping("/{matchingPostId}")
    public ResponseEntity<ApiResponse<Void>> deleteMatchingPost(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long matchingPostId) {

        matchingPostService.removeMatchingPost(memberDetail.getMember(), matchingPostId);
        return ApiResponse.noContent();
    }

    @Operation(summary = "매칭 게시글 다건 조회 (검색)")
    @GetMapping
    public ResponseEntity<ApiResponse<MatchingPostCursorResponse>> getFilteredMatchingPosts(
            @RequestParam(required = false) String artistType,
            @RequestParam(required = false) String workType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false) Long lastViewCount,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        MatchingPostCursorResponse response = matchingPostService.findFilteredMatchingPosts(
                artistType,
                workType,
                keyword,
                lastViewCount,
                lastId,
                pageSize);
        return ApiResponse.success(response);
    }

    @Operation(summary = "검색시 키워드 자동완성")
    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<String>>> getAutocomplete(@RequestParam String keyword) {
        List<String> autocompleteSuggestions = elasticAutocompleteService.findAutocompleteSuggestion(keyword);
        return ApiResponse.success(autocompleteSuggestions);
    }

}
