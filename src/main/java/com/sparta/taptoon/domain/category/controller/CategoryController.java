package com.sparta.taptoon.domain.category.controller;

import com.sparta.taptoon.domain.category.entity.Category;
import com.sparta.taptoon.domain.category.enums.Genre;
import com.sparta.taptoon.domain.category.service.CategoryService;
import com.sparta.taptoon.domain.member.dto.MemberDetail;
import com.sparta.taptoon.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Category", description = "카테고리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "멤버의 카테고리(태그) 추가")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addMemberCategory(@AuthenticationPrincipal MemberDetail memberDetail, @RequestParam Genre genre) {
        categoryService.addMemberCategory(memberDetail.getId(), genre);
        return ApiResponse.noContent();
    }

    @Operation(summary = "멤버의 카테고리 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> findMemberCategory(@AuthenticationPrincipal MemberDetail memberDetail) {
        List<Category> memberCategory = categoryService.findMemberCategory(memberDetail.getId());
        return ApiResponse.success(memberCategory);
    }

    @Operation(summary = "멤버의 카테고리 제거")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteMemberCategory(@AuthenticationPrincipal MemberDetail memberDetail, Genre genre) {
        categoryService.removeMemberCategory(memberDetail.getId(), genre);
        return ApiResponse.noContent();
    }

}
