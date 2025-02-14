package com.sparta.taptoon.domain.category.controller;

import com.sparta.taptoon.domain.category.enums.Genre;
import com.sparta.taptoon.domain.category.service.CategoryService;
import com.sparta.taptoon.domain.member.entity.MemberDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public void addMemberCategory(@AuthenticationPrincipal MemberDetail memberDetail, @RequestParam Genre genre) {
        categoryService.addMemberCategory(memberDetail.getId(), genre);
    }

    @GetMapping
    public void findMemberCategory(@AuthenticationPrincipal MemberDetail memberDetail) {
        categoryService.findMemberCategory(memberDetail.getId());
    }

    @DeleteMapping
    public void deleteMemberCategory(@AuthenticationPrincipal MemberDetail memberDetail, Genre genre) {
        categoryService.removeMemberCategory(memberDetail.getId(), genre);
    }

}
