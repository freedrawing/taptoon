package com.sparta.taptoon.domain.portfolio.service;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.domain.portfolio.dto.request.CreatePortfolioRequest;
import com.sparta.taptoon.domain.portfolio.dto.request.UpdatePortfolioRequest;
import com.sparta.taptoon.domain.portfolio.dto.response.PortfolioImageResponse;
import com.sparta.taptoon.domain.portfolio.dto.response.PortfolioResponse;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import com.sparta.taptoon.domain.portfolio.entity.PortfolioImage;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioImageRepository;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioRepository;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
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
    private final PortfolioImageRepository portfolioImageRepository;

    // 포트폴리오 생성
    @Transactional
    public PortfolioResponse makePortfolio(CreatePortfolioRequest createPortfolioRequest, Long memberId) {

        Member member = memberRepository.findById(memberId)
                        .orElseThrow(()-> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 포트폴리오 몇개 만들었는지 보기
        int countPortfolio = portfolioRepository.countByMemberId(memberId);

        //포트폴리오는 최대 5개까지 생성 가능
        if (5 <= countPortfolio) {
            throw new CreationLimitExceededException(ErrorCode.CREATION_LIMIT_EXCEEDED);
        }

        Portfolio portfolio = createPortfolioRequest.toEntity(member);

        // 포트폴리오 내용 저장
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        // 포트폴리오 이미지 s3에서 가져오기
        List<String> imageUrls = createPortfolioRequest.imageUrls();

        // 등록된 모든 포트폴리오 이미지url을 객체로 변환
        List<PortfolioImage> portfolioImages = imageUrls.stream()
                .map(imageUrl -> new PortfolioImage(imageUrl, portfolio))
                .collect(Collectors.toList());

        // 객체화한 포트폴리오 이미지리스들 레포지토리에 저장
        portfolioImageRepository.saveAll(portfolioImages);

        // 포트폴리오 이미지 responseDto에 담기
        List<PortfolioImageResponse> portfolioImageResponses = portfolioImages.stream()
                .map(portfolioImage -> PortfolioImageResponse.from(portfolioImage))
                .collect(Collectors.toList());

        return PortfolioResponse.from(savedPortfolio, portfolioImageResponses);
    }


    // 포트폴리오 단건 조회
    public PortfolioResponse findPortfolio(Long portfolioId) {

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PORTFOLIO_NOT_FOUND));

        // 포트폴리오 이미지를 포트폴리오 id로 찾기
        List<PortfolioImage> portfolioImages = portfolioImageRepository.findByPortfolioId(portfolioId);
        // 포트폴리오 이미지 responseDto에 담기
        List<PortfolioImageResponse> portfolioImageResponses = portfolioImages.stream()
                .map(portfolioImage -> PortfolioImageResponse.from(portfolioImage))
                .collect(Collectors.toList());

        return PortfolioResponse.from(portfolio, portfolioImageResponses);
    }


    // 포트폴리오 전체 조회
    public List<PortfolioResponse> findAllPortfolio(Long memberId) {

        //포트폴리오 찾기
        List<Portfolio> portfolios = portfolioRepository.findAllByMemberId(memberId);

        // 등록된 모든 포트폴리오 조회
        List<PortfolioResponse> portfolioResponses = portfolios.stream()
                .map(portfolio -> PortfolioResponse.from(portfolio))
                .collect(Collectors.toList());

        return portfolioResponses;
    }


    // 포트폴리오 수정
    @Transactional
    public PortfolioResponse editPortfolio(
            UpdatePortfolioRequest updatePortfolioRequest, Long portfolioId, Long memberId, Long portfolioImageId) {

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
        portfolio.updatePortfolio(updatePortfolioRequest);

        // 포트폴리오 이미지를 삭제하는 로직
        // 포트폴리오 이미지 삭제 요청이 있다
        if (updatePortfolioRequest.portfolioImageIdsToDel() !=null && !updatePortfolioRequest.portfolioImageIdsToDel().isEmpty()) {
            // 삭제할 포트폴리오 이미지 아이디 조회
            for (Long portfolioImageIds : updatePortfolioRequest.portfolioImageIdsToDel()) {
                // 요청받은 삭제할 포트폴리오 이미지 아이디를 레포지토리에서 찾기
                PortfolioImage portfolioImageToDel = portfolioImageRepository.findById(portfolioImageIds)
                        .orElseThrow(() -> new NotFoundException(ErrorCode.PORTFOLIO_IMAGE_NOT_FOUND));
                // 삭제하려는 포트폴리오 이미지의 포트폴리오 id와 포트폴리오 id가 같은지 검증
                if (!portfolioImageToDel.getPortfolio().getId().equals(portfolioId)) {
                   throw new AccessDeniedException(ErrorCode.PORTFOLIO_IMAGE_ACCESS_DENIED);
                }
                // 포트폴리오 이미지 db로부터 삭제
                portfolioImageRepository.delete(portfolioImageToDel);
            }
        }
        return PortfolioResponse.from(portfolio);
    }


    // 포트폴리오 삭제
    @Transactional
    public void removePortfolio(Long portfolioId, Long memberId) {

        // 유저 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 삭제할 포트폴리오 Id로 찾기
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PORTFOLIO_NOT_FOUND));

        // 삭제할 포트폴리오가 유저의 포트폴리오인지 검사
        if(!portfolio.getMember().equals(member)) {
            throw new NotFoundException(ErrorCode.PORTFOLIO_ACCESS_DENIED);
        }

        // 포트폴리오 삭제 (boolean type true로 변경)
        portfolio.remove();

    }
}
