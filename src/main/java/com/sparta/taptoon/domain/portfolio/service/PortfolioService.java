package com.sparta.taptoon.domain.portfolio.service;

import com.sparta.taptoon.domain.portfolio.dto.response.CreatePortfolioResponse;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public CreatePortfolioResponse createPortfolio() {

    }



}
