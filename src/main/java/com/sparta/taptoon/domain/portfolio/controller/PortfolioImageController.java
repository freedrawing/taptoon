package com.sparta.taptoon.domain.portfolio.controller;

import com.sparta.taptoon.domain.portfolio.dto.portfolioImageDto.response.GetPortfolioImageResponse;
import com.sparta.taptoon.domain.portfolio.service.PortfolioImageService;
import com.sparta.taptoon.global.common.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/portfolio_images")
public class PortfolioImageController {

    private final PortfolioImageService portfolioImageService;

    /**
     *
     * @param portfolioId
     * @return
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<GetPortfolioImageResponse>>> getPortfolioImage(@RequestParam("portfolio_id") Long portfolioId) {
        List<GetPortfolioImageResponse> portfolioImage = portfolioImageService.findPortfolioImage(portfolioId);
        return ApiResponse.success(portfolioImage);
    }
}
