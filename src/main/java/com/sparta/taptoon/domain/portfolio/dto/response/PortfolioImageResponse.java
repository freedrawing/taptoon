package com.sparta.taptoon.domain.portfolio.dto.response;

import com.sparta.taptoon.domain.portfolio.entity.PortfolioImage;
import lombok.Builder;

@Builder
public record PortfolioImageResponse(
        Long portfolioImgId,
        String imageUrl
) {

    @Builder
    public static PortfolioImageResponse from(PortfolioImage portfolioImage) {
        return PortfolioImageResponse.builder()
                .portfolioImgId(portfolioImage.getId())
                .imageUrl(portfolioImage.getImageUrl())
                .build();
    }
}
