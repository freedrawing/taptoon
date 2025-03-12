package com.sparta.taptoon.domain.portfolio.controller;

import com.sparta.taptoon.domain.member.dto.MemberDetail;
import com.sparta.taptoon.domain.portfolio.dto.request.RegisterPortfolioRequest;
import com.sparta.taptoon.domain.portfolio.dto.request.UpdatePortfolioRequest;
import com.sparta.taptoon.domain.portfolio.dto.response.PortfolioResponse;
import com.sparta.taptoon.domain.portfolio.service.PortfolioService;
import com.sparta.taptoon.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Portfolio", description = "포트폴리오 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Operation(summary =  "포트폴리오 시작하기")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> startPortfolio(@AuthenticationPrincipal MemberDetail memberDetail) {

        Long portfolioId = portfolioService.startPortfolio(memberDetail.getMember());
        return ApiResponse.created(portfolioId);
    }

    @Operation(summary = "포트폴리오 내용 채우기 (제목, 내용, 파일)")
    @PutMapping("/{portfolioId}/registration")
    public ResponseEntity<ApiResponse<PortfolioResponse>> fillInPortfolio(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @Valid @RequestBody RegisterPortfolioRequest request,
            @PathVariable Long portfolioId) {
        PortfolioResponse portfolio = portfolioService.registerPortfolio(request, memberDetail.getMember(), portfolioId);
        return ApiResponse.success(portfolio);
    }

    @Operation(summary = "유저 한 명이 등록한 모든 포트폴리오 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PortfolioResponse>>> getUserPortfolios(@RequestParam Long memberId) {
        List<PortfolioResponse> myAllPortfolios = portfolioService.findAllPortfoliosBy(memberId);
        return ApiResponse.success(myAllPortfolios);
    }

    @Operation(summary = "포트폴리오 상세조회")
    @GetMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<PortfolioResponse>> getPortfolio(@PathVariable Long portfolioId) {
        PortfolioResponse portfolio = portfolioService.findPortfolio(portfolioId);
        return ApiResponse.success(portfolio);
    }

    @Operation(summary = "포트폴리오 편집")
    @PutMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<Void>> updatePortfolio(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long portfolioId,
            @Valid @RequestBody UpdatePortfolioRequest request) {

        portfolioService.editPortfolio(portfolioId, memberDetail.getMember(), request);
        return ApiResponse.noContent();
    }

    @Operation(summary = "포트폴리오 삭제")
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<Void>> deletePortfolio(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long portfolioId) {
        portfolioService.removePortfolio(portfolioId,memberDetail.getMember());
        return ApiResponse.noContent();
    }
}
