package com.sparta.taptoon.domain.portfolio.dto.response;

import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import com.sparta.taptoon.domain.portfolio.entity.PortfolioImage;
import lombok.Builder;

@Builder
public record PortfolioImageResponse(
        Portfolio portfolio,
        Long portfolioImgId,
        String imgUrl
) {

    @Builder
    public static PortfolioImageResponse from(PortfolioImage portfolioImage) {
        return PortfolioImageResponse.builder()
                .portfolio(portfolioImage.getPortfolio())
                .portfolioImgId(portfolioImage.getId())
                .imgUrl(portfolioImage.getImageUrl())
                .build();
    }
}
