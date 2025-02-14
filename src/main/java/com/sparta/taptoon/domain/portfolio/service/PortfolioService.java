package com.sparta.taptoon.domain.portfolio.service;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.domain.portfolio.dto.request.CreatePortfolioRequest;
import com.sparta.taptoon.domain.portfolio.dto.response.CreatePortfolioResponse;
import com.sparta.taptoon.domain.portfolio.dto.response.GetAllPortfolioResponse;
import com.sparta.taptoon.domain.portfolio.dto.response.GetPortfolioResponse;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

        // 포트폴리오 몇개 만들었는지 보기
        int countPortfolio = portfolioRepository.countByMember(member);

        //포트폴리오는 최대 5개까지 생성 가능
        if (countPortfolio > 5) {
            throw new IllegalArgumentException("포트폴리오는 5개까지만 만들 수 있습니다.");
        }

        Portfolio portfolio = createPortfolioRequest.toEntity(member);

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        return CreatePortfolioResponse.from(savedPortfolio);
    }

    // 포트폴리오 단건 조회
    public GetPortfolioResponse findPortfolio(Long portfolioId) {

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("포트폴리오를 찾을 수 없습니다."));

        return GetPortfolioResponse.from(portfolio);
    }

    // 포트폴리오 전체 조회
    public List<GetAllPortfolioResponse> findAllPortfolio(Long portfolioId) {

        //포트폴리오 찾기
        List<Portfolio> portfolios = portfolioRepository.findAllByPortfolioId(portfolioId);

        // 등록된 모든 포트폴리오 조회
        List<GetAllPortfolioResponse> portfolioResponses = portfolios.stream()
                .map(portfolio -> GetAllPortfolioResponse.from(portfolio))
                .collect(Collectors.toList());

        return portfolioResponses;
    }


}
