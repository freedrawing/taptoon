package com.sparta.taptoon.domain.portfolio.service;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.domain.portfolio.dto.request.PortfolioRequest;
import com.sparta.taptoon.domain.portfolio.dto.response.PortfolioResponse;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioRepository;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.CreationLimitExceededException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
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
    public PortfolioResponse makePortfolio(PortfolioRequest portfolioRequest, Long memberId) {

        Member member = memberRepository.findById(memberId)
                        .orElseThrow(()-> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 포트폴리오 몇개 만들었는지 보기
        int countPortfolio = portfolioRepository.countByMember(member);

        //포트폴리오는 최대 5개까지 생성 가능
        if (countPortfolio > 5) {
            throw new CreationLimitExceededException(ErrorCode.CREATION_LIMIT_EXCEEDED);
        }

        Portfolio portfolio = portfolioRequest.toEntity(member);

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        return PortfolioResponse.from(savedPortfolio);
    }

    // 포트폴리오 단건 조회
    public PortfolioResponse findPortfolio(Long portfolioId) {

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        return PortfolioResponse.from(portfolio);
    }

    // 포트폴리오 전체 조회
    public List<PortfolioResponse> findAllPortfolio(Long portfolioId) {

        //포트폴리오 찾기
        List<Portfolio> portfolios = portfolioRepository.findAllByPortfolioId(portfolioId);

        // 등록된 모든 포트폴리오 조회
        List<PortfolioResponse> portfolioResponses = portfolios.stream()
                .map(portfolio -> PortfolioResponse.from(portfolio))
                .collect(Collectors.toList());

        return portfolioResponses;
    }

    // 포트폴리오 수정
    @Transactional
    public PortfolioResponse editPortfolio(PortfolioRequest portfolioRequest, Long portfolioId, Long memberId) {

        // 유저 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 수정할 포트폴리오 Id로 찾기
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PORTFOLIO_NOT_FOUND));

        // 수정할 포트폴리오가 유저의 포트폴리오인지 검사
        if(!portfolio.getMember().equals(member)) {
            throw new NotFoundException(ErrorCode.PORTFOLIO_ACCESS_DENIED);
        }

        // 포트폴리오 엔티티의 수정 메서드로 받은 request 값대로 수정하기
        portfolio.update(portfolioRequest);

        // 수정한 포트폴리오 반환
        return PortfolioResponse.from(portfolio);
    }

    // 포트폴리오 삭제
    @Transactional
    public void removePortfolio(Long portfolioId, Long memberId) {

        // 유저 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 수정할 포트폴리오 Id로 찾기
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PORTFOLIO_NOT_FOUND));

        // 수정할 포트폴리오가 유저의 포트폴리오인지 검사
        if(!portfolio.getMember().equals(member)) {
            throw new NotFoundException(ErrorCode.PORTFOLIO_ACCESS_DENIED);
        }

        // 포트폴리오 삭제 (boolean type true로 변경)
        portfolio.remove();

    }
}
