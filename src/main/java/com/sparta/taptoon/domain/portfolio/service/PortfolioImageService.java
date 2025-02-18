package com.sparta.taptoon.domain.portfolio.service;

import com.sparta.taptoon.domain.portfolio.dto.portfolioImageDto.response.GetPortfolioImageResponse;
import com.sparta.taptoon.domain.portfolio.entity.PortfolioImage;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioImageRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class PortfolioImageService {

    private final PortfolioImageRepository portfolioImageRepository;

    public List<GetPortfolioImageResponse> findPortfolioImage(Long portfolioId) {

        // 포트폴리오 이미지 포트폴리오 아이디로 찾기
        List<PortfolioImage> portfolioImages = portfolioImageRepository.findByPortfolioId(portfolioId);

        // 포트폴리오 이미지
        List<GetPortfolioImageResponse> portfolioImageResponses = portfolioImages.stream()
                .map(portfolioImage -> GetPortfolioImageResponse.from(portfolioImage))
                .collect(Collectors.toList());

        return portfolioImageResponses;
    }

}
