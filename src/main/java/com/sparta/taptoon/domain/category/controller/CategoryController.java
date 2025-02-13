package com.sparta.taptoon.domain.category.controller;

import com.sparta.taptoon.domain.category.enums.Genre;
import com.sparta.taptoon.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/{id}")
    public void addMemberCategory(@PathVariable Long id, @RequestParam Genre genre) {
        categoryService.addMemberCategory(id,genre);
    }

    @GetMapping
    public void findMemberCategory(Long memberId) {
        categoryService.findMemberCategory(memberId);
    }

    @DeleteMapping
    public void deleteMemberCategory(Long memberId, Genre genre) {
        categoryService.removeMemberCategory(memberId,genre);
    }

}
