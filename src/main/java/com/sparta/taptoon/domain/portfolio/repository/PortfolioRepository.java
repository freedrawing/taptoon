package com.sparta.taptoon.domain.portfolio.repository;

import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    // 등록된 포트폴리오 개수
    @Query("SELECT COUNT(p) FROM Portfolio p WHERE p.owner.id = :ownerId " +
            "AND p.status = com.sparta.taptoon.global.common.enums.Status.REGISTERED")
    int countPortfoliosByOwnerId(@Param("ownerId") Long ownerId);

    // 유저의 현재 등록된 모든 포트폴리오 가져오기
    @Query("SELECT DISTINCT p FROM Portfolio p " +
            "LEFT JOIN p.portfolioFiles pf " +
            "WHERE p.owner.id = :ownerId " +
            "AND p.status = com.sparta.taptoon.global.common.enums.Status.REGISTERED " +
            "AND (pf.status = com.sparta.taptoon.global.common.enums.Status.REGISTERED OR pf IS NULL)")
    List<Portfolio> findAllWithFilesByOwnerIdAndRegisteredStatus(@Param("ownerId") Long ownerId);
}
