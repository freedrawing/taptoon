package com.sparta.taptoon.domain.portfolio.controller;

import com.sparta.taptoon.domain.portfolio.dto.request.CreatePortfolioRequest;
import com.sparta.taptoon.domain.portfolio.dto.request.UpdatePortfolioRequest;
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
    public ResponseEntity<ApiResponse<PortfolioResponse>> createPortfolio(
            @Valid @RequestBody CreatePortfolioRequest portfolioRequest, Long memberId) {
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
    public ResponseEntity<ApiResponse<Void>> updatePortfolio(
            @Valid @PathVariable Long portfolioId,
            @RequestBody UpdatePortfolioRequest updatePortfolioRequest, Long memberId) {
        // 수정 필요
        portfolioService.editPortfolio(updatePortfolioRequest, portfolioId, memberId);
        return ApiResponse.noContentAndSendMessage("성공적으로 수정되었습니다.");
    }

    @Operation(summary = "포트폴리오 삭제")
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<Void>> deletePortfolio(@PathVariable Long portfolioId, Long memberId) {
        portfolioService.removePortfolio(portfolioId,memberId);
        return ApiResponse.noContentAndSendMessage("성공적으로 삭제되었습니다.");
    }
}
