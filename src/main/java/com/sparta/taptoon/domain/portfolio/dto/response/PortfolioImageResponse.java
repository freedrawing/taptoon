package com.sparta.taptoon.domain.portfolio.dto.response;

import com.sparta.taptoon.domain.portfolio.entity.PortfolioImage;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PortfolioImageResponse(
        Long portfolioImageId,
        Long portfolioId,
        String fileUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    @Builder
    public static PortfolioImageResponse from(PortfolioImage portfolioImage) {
        return PortfolioImageResponse.builder()
                .portfolioImageId(portfolioImage.getId())
                .portfolioId(portfolioImage.getPortfolio().getId())
                .fileUrl(portfolioImage.getFileUrl())
                .createdAt(portfolioImage.getCreatedAt())
                .updatedAt(portfolioImage.getUpdatedAt())
                .build();
    }
}
