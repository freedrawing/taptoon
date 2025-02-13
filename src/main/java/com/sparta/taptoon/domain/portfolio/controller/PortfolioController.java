package com.sparta.taptoon.domain.portfolio.controller;

import com.sparta.taptoon.domain.portfolio.dto.request.CreatePortfolioRequest;
import com.sparta.taptoon.domain.portfolio.dto.response.CreatePortfolioResponse;
import com.sparta.taptoon.domain.portfolio.service.PortfolioService;
import com.sparta.taptoon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    // 포트폴리오 생성
    @PostMapping
    public ResponseEntity<ApiResponse<CreatePortfolioResponse>> createPortfolio(@RequestBody CreatePortfolioRequest createPortfolioRequest, Long memberId) {
        CreatePortfolioResponse portfolio = portfolioService.createPortfolio(createPortfolioRequest, memberId);
        return ApiResponse.created(portfolio);
    }
}
