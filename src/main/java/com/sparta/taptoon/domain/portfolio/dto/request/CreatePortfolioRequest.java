package com.sparta.taptoon.domain.portfolio.dto.request;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;


public record CreatePortfolioRequest(Member member, String title, String content, String fileUrl) {

    public Portfolio toEntity(Member member) {
        return Portfolio.builder()
                .member(member)
                .title(title)
                .content(content)
                .fileUrl(fileUrl)
                .build();
    }
}
