package com.sparta.taptoon.domain.portfolio.dto.response;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CreatePortfolioResponse(Member member, String content, LocalDateTime createdAt) {

    public static CreatePortfolioResponse from(Portfolio portfolio) {
        return CreatePortfolioResponse.builder()
                .member(portfolio.getMember())
                .content(portfolio.getContent())
                .createdAt(portfolio.getCreatedAt())
                .build();
    }
}
