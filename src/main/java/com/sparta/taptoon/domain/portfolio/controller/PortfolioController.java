package com.sparta.taptoon.domain.portfolio.controller;

import com.sparta.taptoon.domain.portfolio.dto.portfolioDto.request.PortfolioRequest;
import com.sparta.taptoon.domain.portfolio.dto.portfolioDto.response.CreatePortfolioResponse;
import com.sparta.taptoon.domain.portfolio.dto.portfolioDto.response.GetAllPortfolioResponse;
import com.sparta.taptoon.domain.portfolio.dto.portfolioDto.response.GetPortfolioResponse;
import com.sparta.taptoon.domain.portfolio.dto.portfolioDto.response.UpdatePortfolioResponse;
import com.sparta.taptoon.domain.portfolio.service.PortfolioService;
import com.sparta.taptoon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    // 포트폴리오 생성
    @PostMapping
    public ResponseEntity<ApiResponse<CreatePortfolioResponse>> createPortfolio(@RequestBody PortfolioRequest portfolioRequest, Long memberId) {
        CreatePortfolioResponse portfolio = portfolioService.makePortfolio(portfolioRequest, memberId);
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

    // 포트폴리오 수정
    @PutMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<UpdatePortfolioResponse>> updatePortfolio(@RequestBody PortfolioRequest portfolioRequest, Long portfolioId, Long memberId) {
        UpdatePortfolioResponse updatePortfolioResponse = portfolioService.editPortfolio(portfolioRequest, portfolioId, memberId);
        return ApiResponse.success(updatePortfolioResponse);
    }

    // 포트폴리오 삭제
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<Void>> deletePortfolio(@PathVariable Long portfolioId, Long memberId) {
        portfolioService.removePortfolio(portfolioId,memberId);
        return ApiResponse.success(null);
    }
}
