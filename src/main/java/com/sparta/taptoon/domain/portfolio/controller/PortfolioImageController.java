package com.sparta.taptoon.domain.portfolio.controller;

import com.sparta.taptoon.domain.portfolio.dto.response.PortfolioImageResponse;
import com.sparta.taptoon.domain.portfolio.service.PortfolioImageService;
import com.sparta.taptoon.global.common.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/portfolio_images")
public class PortfolioImageController {

    private final PortfolioImageService portfolioImageService;

    // 포트폴리오 이미지 리스트로 조회
    @GetMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<List<PortfolioImageResponse>>> getPortfolioImage(@PathVariable Long portfolioId) {
        List<PortfolioImageResponse> portfolioImage = portfolioImageService.findPortfolioImage(portfolioId);
        return ApiResponse.success(portfolioImage);
    }
}
