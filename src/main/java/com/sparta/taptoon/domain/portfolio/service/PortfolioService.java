package com.sparta.taptoon.domain.portfolio.service;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.domain.portfolio.dto.request.PortfolioRequest;
import com.sparta.taptoon.domain.portfolio.dto.response.PortfolioImageResponse;
import com.sparta.taptoon.domain.portfolio.dto.response.PortfolioResponse;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import com.sparta.taptoon.domain.portfolio.entity.PortfolioImage;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioImageRepository;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioRepository;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.CreationLimitExceededException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final MemberRepository memberRepository;
    private final PortfolioImageRepository portfolioImageRepository;

    // 포트폴리오 판떼기 만들기
    @Transactional
    public Long startPortfolio(Member member) {
        // 포트폴리오 몇개 만들었는지 보기
        int countPortfolio = portfolioRepository.countByMemberId(member.getId());

        // 구독을 하면 등급에 따라 최대 생성 개수 증가 기능 고려해보기
        // 포트폴리오는 최대 5개까지 생성 가능
        if (5 <= countPortfolio) {
            throw new CreationLimitExceededException(ErrorCode.CREATION_LIMIT_EXCEEDED);
        }

        // 빈 포트폴리오 만들기
        Portfolio portfolio = Portfolio.builder()
                .member(member)
                .title("")
                .content("")
                .fileUrl("")
                .build();
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        return savedPortfolio.getId();
    }


    // 포트폴리오 내용 채우기 (제목, 내용, 파일)
    @Transactional
    public PortfolioResponse fillInPortfolio(PortfolioRequest portfolioRequest, Member member, Long portfolioId) {
        // 내용 채울 포트폴리오 찾기
        Portfolio foundPortfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PORTFOLIO_NOT_FOUND));

        //  제목 + 내용 기입하기
        foundPortfolio.updatePortfolio(portfolioRequest);

        // 이미지 등록
        List<PortfolioImageResponse> portfolioFileResponses = Collections.emptyList();
        if (!portfolioRequest.fileIds().isEmpty()) {
            // 레포지에 등록된 파일 모두 찾기
            List<PortfolioImage> foundFiles = portfolioImageRepository.findAllById(portfolioRequest.fileIds());
            // pending에서 registered로 상태 변환
            foundFiles.forEach(PortfolioImage::updateStatus);

            List<PortfolioImageResponse> portfolioImageResponses = foundFiles.stream()
                    .map(PortfolioImageResponse::from) // Status registered로 변경
                    .collect(Collectors.toList());
        }
        return PortfolioResponse.from(foundPortfolio, portfolioFileResponses);
    }


    // 포트폴리오 단건 조회
    public PortfolioResponse findPortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PORTFOLIO_NOT_FOUND));

        // 포트폴리오 이미지를 포트폴리오 id로 찾기
        List<PortfolioImage> portfolioImages = portfolioImageRepository.findByPortfolioId(portfolioId);
        // 포트폴리오 이미지 responseDto에 담기
        List<PortfolioImageResponse> portfolioImageResponses = portfolioImages.stream()
                .map(PortfolioImageResponse::from)
                .collect(Collectors.toList());

        return PortfolioResponse.from(portfolio, portfolioImageResponses);
    }


    // 포트폴리오 삭제
    @Transactional
    public void removePortfolio(Long portfolioId, Member member) {

        // 삭제할 포트폴리오 Id로 찾기
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PORTFOLIO_NOT_FOUND));

        // 삭제할 포트폴리오가 유저의 포트폴리오인지 검사
        if(!portfolio.getMember().getId().equals(member.getId())) {
            throw new NotFoundException(ErrorCode.PORTFOLIO_ACCESS_DENIED);
        }

        // 포트폴리오 삭제 (boolean type true로 변경)
        portfolio.remove();

    }
}
