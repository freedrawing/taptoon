package com.sparta.taptoon.domain.category.repository;

import com.sparta.taptoon.domain.category.entity.Category;
import com.sparta.taptoon.domain.category.enums.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    void deleteByMemberIdAndGenre(Long memberId, Genre genre);
    List<Category> findAllByMemberId(Long id);
}
