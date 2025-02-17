package com.sparta.taptoon.domain.portfolio.dto.portfolioDto.response;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import lombok.Builder;

import java.time.LocalDateTime;


@Builder
public record GetAllPortfolioResponse(Member member, String title, LocalDateTime createdAt) {

    public static GetAllPortfolioResponse from(Portfolio portfolio) {
        return GetAllPortfolioResponse.builder()
                .member(portfolio.getMember())
                .title(portfolio.getTitle())
                .createdAt(portfolio.getCreatedAt())
                .build();
    }

}
