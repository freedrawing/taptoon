package com.sparta.taptoon.domain.portfolio.dto.portfolioImageDto.response;

import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import com.sparta.taptoon.domain.portfolio.entity.PortfolioImage;
import lombok.Builder;

@Builder
public record GetPortfolioImageResponse(Portfolio portfolio, String imgUrl) {

    @Builder
    public static GetPortfolioImageResponse from(PortfolioImage portfolioImage) {
        return GetPortfolioImageResponse.builder()
                .portfolio(portfolioImage.getPortfolio())
                .imgUrl(portfolioImage.getImageUrl())
                .build();
    }
}
