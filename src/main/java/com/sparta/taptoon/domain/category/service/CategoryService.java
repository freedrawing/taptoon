package com.sparta.taptoon.domain.category.service;

import com.sparta.taptoon.domain.category.entity.Category;
import com.sparta.taptoon.domain.category.enums.Genre;
import com.sparta.taptoon.domain.category.repository.CategoryRepository;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    public void addMemberCategory(Long memberId, Genre genre) {
        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundException::new);
        Category category = new Category(member,genre);
        categoryRepository.save(category);
    }

    public List<Category> findMemberCategory(Long memberId) {
        return categoryRepository.findAllByMemberId(memberId);
    }

    @Transactional
    public void removeMemberCategory(Long memberId, Genre genre) {
        categoryRepository.deleteByMemberIdAndGenre(memberId,genre);
    }

}
