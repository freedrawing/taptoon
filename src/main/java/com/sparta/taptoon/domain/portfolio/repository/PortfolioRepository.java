package com.sparta.taptoon.domain.portfolio.repository;

import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findAllByPortfolioId(Long portfolioId);
}
