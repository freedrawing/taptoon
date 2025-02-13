package com.sparta.taptoon.domain.portfolio.service;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.domain.portfolio.dto.request.CreatePortfolioRequest;
import com.sparta.taptoon.domain.portfolio.dto.response.CreatePortfolioResponse;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final MemberRepository memberRepository;

    // 포트폴리오 생성
    @Transactional
    public CreatePortfolioResponse createPortfolio(CreatePortfolioRequest createPortfolioRequest, Long memberId) {
        Member member = memberRepository.findById(memberId)
                        .orElseThrow(()-> new RuntimeException("유저 정보를 찾을 수 없습니다."));
        Portfolio portfolio = createPortfolioRequest.toEntity(member);
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        return CreatePortfolioResponse.from(savedPortfolio);
    }



}
