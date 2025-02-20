package com.sparta.taptoon.domain.portfolio.controller;

import com.sparta.taptoon.domain.portfolio.dto.request.PortfolioRequest;
import com.sparta.taptoon.domain.portfolio.dto.response.PortfolioResponse;
import com.sparta.taptoon.domain.portfolio.service.PortfolioService;
import com.sparta.taptoon.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Portfolio", description = "포트폴리오 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Operation(summary = "포트폴리오 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<PortfolioResponse>> createPortfolio(@Valid @RequestBody PortfolioRequest portfolioRequest, Long memberId) {
        PortfolioResponse portfolio = portfolioService.makePortfolio(portfolioRequest, memberId);
        return ApiResponse.created(portfolio);
    }

    @Operation(summary = "포트폴리오 상세조회")
    @GetMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<PortfolioResponse>> getPortfolio(@PathVariable Long portfolioId) {
        PortfolioResponse portfolio = portfolioService.findPortfolio(portfolioId);
        return ApiResponse.success(portfolio);
    }

    @Operation(summary = "포트폴리오 전체조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PortfolioResponse>>> getAllPortfolio(Long memberId) {
        List<PortfolioResponse> portfolios = portfolioService.findAllPortfolio(memberId);
        return ApiResponse.success(portfolios);
    }

    @Operation(summary = "포트폴리오 수정")
    @PutMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<PortfolioResponse>> updatePortfolio(@RequestBody PortfolioRequest portfolioRequest, Long portfolioId, Long memberId) {
        PortfolioResponse updatePortfolioResponse = portfolioService.editPortfolio(portfolioRequest, portfolioId, memberId);
        return ApiResponse.success(updatePortfolioResponse);
    }

    @Operation(summary = "포트폴리오 삭제")
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<Void>> deletePortfolio(@PathVariable Long portfolioId, Long memberId) {
        portfolioService.removePortfolio(portfolioId,memberId);
        return ApiResponse.success(null);
    }
}
