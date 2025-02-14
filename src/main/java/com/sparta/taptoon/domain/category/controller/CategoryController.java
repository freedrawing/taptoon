package com.sparta.taptoon.domain.category.controller;

import com.sparta.taptoon.domain.category.enums.Genre;
import com.sparta.taptoon.domain.category.service.CategoryService;
import com.sparta.taptoon.domain.member.entity.MemberDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Category", description = "카테고리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "멤버의 카테고리(태그) 추가")
    @PostMapping
    public void addMemberCategory(@AuthenticationPrincipal MemberDetail memberDetail, @RequestParam Genre genre) {
        categoryService.addMemberCategory(memberDetail.getId(), genre);
    }

    @Operation(summary = "멤버의 카테고리 조회")
    @GetMapping
    public void findMemberCategory(@AuthenticationPrincipal MemberDetail memberDetail) {
        categoryService.findMemberCategory(memberDetail.getId());
    }

    @Operation(summary = "멤버의 카테고리 제거")
    @DeleteMapping
    public void deleteMemberCategory(@AuthenticationPrincipal MemberDetail memberDetail, Genre genre) {
        categoryService.removeMemberCategory(memberDetail.getId(), genre);
    }

}
