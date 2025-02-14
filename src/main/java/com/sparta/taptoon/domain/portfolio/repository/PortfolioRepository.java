package com.sparta.taptoon.domain.portfolio.repository;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    // 포트폴리오 생성 개수 세기
    int countByMember(Member member);

    // 전체 포트폴리오 Id값 가져오는 메서드
    List<Portfolio> findAllByPortfolioId(Long portfolioId);
}
