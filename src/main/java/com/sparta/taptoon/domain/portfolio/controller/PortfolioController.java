package com.sparta.taptoon.domain.portfolio.controller;

import com.sparta.taptoon.domain.portfolio.dto.request.CreatePortfolioRequest;
import com.sparta.taptoon.domain.portfolio.dto.response.CreatePortfolioResponse;
import com.sparta.taptoon.domain.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping
    public ResponseEntity<CreatePortfolioResponse> createPortfolio(@RequestBody CreatePortfolioRequest createPortfolioRequest) {

    }
}
