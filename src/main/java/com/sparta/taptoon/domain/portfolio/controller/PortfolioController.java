package com.sparta.taptoon.domain.portfolio.controller;

import com.sparta.taptoon.domain.portfolio.dto.request.CreatePortfolioRequest;
import com.sparta.taptoon.domain.portfolio.dto.response.CreatePortfolioResponse;
import com.sparta.taptoon.domain.portfolio.dto.response.GetAllPortfolioResponse;
import com.sparta.taptoon.domain.portfolio.dto.response.GetPortfolioResponse;
import com.sparta.taptoon.domain.portfolio.service.PortfolioService;
import com.sparta.taptoon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // 포트폴리오 단건 조회
    @GetMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<GetPortfolioResponse>> getPortfolio(@PathVariable Long portfolioId) {
        GetPortfolioResponse portfolio = portfolioService.findPortfolio(portfolioId);
        return ApiResponse.success(portfolio);
    }

    // 포트폴리오 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<GetAllPortfolioResponse>>> getAllPortfolio(Long portfolioId) {
        List<GetAllPortfolioResponse> portfolios = portfolioService.findAllPortfolio(portfolioId);
        return ApiResponse.success(portfolios);
    }


}
