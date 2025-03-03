package com.sparta.taptoon.domain.portfolio.dto.response;

import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import com.sparta.taptoon.global.common.enums.Status;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public record PortfolioResponse(
        Long portfolioId,
        Long ownerId,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PortfolioFileResponse> fileList
) {
    // PortfolioResponse에 PortfolioFileResponse를 담기
    public static PortfolioResponse from(Portfolio portfolio) {
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getOwner().getId(),
                portfolio.getTitle(),
                portfolio.getContent(),
                portfolio.getCreatedAt(),
                portfolio.getUpdatedAt(),
                portfolio.getPortfolioFiles()
                        .stream()
                        .filter(file -> Status.isRegistered(file.getStatus()))
                        .map(PortfolioFileResponse::from).toList()
        );
    }
}
