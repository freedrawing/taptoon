package com.sparta.taptoon.domain.portfolio.dto.response;

import com.sparta.taptoon.domain.portfolio.entity.PortfolioFile;
import lombok.Builder;

import java.time.LocalDateTime;

public record PortfolioFileResponse(
        Long id,
        Long portfolioId,
        String fileName,
        String fileType,
        String thumbnailUrl,
        String fileUrl
) {

    @Builder
    public static PortfolioFileResponse from(PortfolioFile portfolioFile) {
        return new PortfolioFileResponse(
                portfolioFile.getId(),
                portfolioFile.getPortfolio().getId(),
                portfolioFile.getFileName(),
                portfolioFile.getFileType().name(),
                portfolioFile.getThumbnailUrl(),
                portfolioFile.getFileUrl()
        );
    }
}
