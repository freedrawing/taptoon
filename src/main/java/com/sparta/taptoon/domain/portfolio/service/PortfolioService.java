package com.sparta.taptoon.domain.portfolio.service;

import com.sparta.taptoon.domain.image.service.AwsS3Service;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.portfolio.dto.request.RegisterPortfolioRequest;
import com.sparta.taptoon.domain.portfolio.dto.request.UpdatePortfolioRequest;
import com.sparta.taptoon.domain.portfolio.dto.response.PortfolioResponse;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import com.sparta.taptoon.domain.portfolio.entity.PortfolioFile;
import com.sparta.taptoon.domain.portfolio.enums.FileType;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioFileRepository;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioRepository;
import com.sparta.taptoon.global.common.enums.Status;
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
import com.sparta.taptoon.global.error.exception.CreationLimitExceededException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.sparta.taptoon.global.error.enums.ErrorCode.CREATION_LIMIT_EXCEEDED;
import static com.sparta.taptoon.global.error.enums.ErrorCode.PORTFOLIO_NOT_FOUND;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioFileRepository portfolioFileRepository;
    private final AwsS3Service awsS3Service;

    private final int PORTFOLIO_LIMIT = 5;

    // 포트폴리오 판떼기 만들기
    @Transactional
    public Long startPortfolio(Member member) {
        // 포트폴리오 몇개 만들었는지 보기
        int countPortfolio = portfolioRepository.countPortfoliosByOwnerId(member.getId());

        // 구독을 하면 등급에 따라 최대 생성 개수 증가 기능 고려해보기
        // 포트폴리오는 최대 5개까지 생성 가능
        if (PORTFOLIO_LIMIT <= countPortfolio) {
            throw new CreationLimitExceededException(CREATION_LIMIT_EXCEEDED);
        }
        // 빈 포트폴리오 만들기
        Portfolio portfolio = Portfolio.builder()
                .owner(member)
                .title("")
                .content("")
                .build();
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        return savedPortfolio.getId();
    }

    // 포트폴리오 파일 판떼기 만들기
    @Transactional
    public Long generateEmptyPortfolioFile(Long portfolioId, String fileName, String fileType, String thumbnailImageUrl, String fileUrl) {
        Portfolio findPortfolio = findPortfolioById(portfolioId);

        PortfolioFile savedPortfolioFile = portfolioFileRepository.save(
                PortfolioFile.builder()
                        .portfolio(findPortfolio)
                        .fileName(fileName)
                        .thumbnailUrl(thumbnailImageUrl) // 파일 타입이 이미지면 thumbnailUrl 저장 아니면 null 저장
                        .fileUrl(fileUrl)
                        .fileType(fileType)
                        .build()
        );

        return savedPortfolioFile.getId();
    }


    // 포트폴리오 저장 (수정이지만 사실상 저장)
    @Transactional
    public PortfolioResponse registerPortfolio(RegisterPortfolioRequest request, Member member, Long portfolioId) {
        // 내용 채울 포트폴리오 찾기
        Portfolio findPortfolio = findPortfolioById(portfolioId);
        validatePortfolioAccess(member.getId(), findPortfolio);

        //  포트포리오 및 포트폴리오와 같이 등록한 파일 저장
        findPortfolio.registerPortfolio(request);
        registerPortfolioFiles(request.portfolioFileIds());

        return PortfolioResponse.from(findPortfolio);
    }

    /*
     * PortfolioFile, ID로 PENDING -> REGISTERED로 변경
     * `registerPortfolio()`의 트랜잭션 하에서 실행됨
     */
    private void registerPortfolioFiles(List<Long> portfolioFileIds) {
        if (portfolioFileIds.isEmpty()) return;

        List<PortfolioFile> uploadedPortfolioFiles = portfolioFileRepository.findAllById(portfolioFileIds);
        if (uploadedPortfolioFiles.isEmpty() == false) {
//            uploadedPortfolioFiles.forEach(PortfolioFile::registerMe);
            portfolioFileRepository.updateStatusByIds(portfolioFileIds, Status.REGISTERED);
        }
    }

    // 포트폴리오 진짜 수정
    @Transactional
    public void editPortfolio(Long portfolioId, Member member, UpdatePortfolioRequest request) {
        Portfolio findPortfolio = findPortfolioById(portfolioId);
        validatePortfolioAccess(member.getId(), findPortfolio);

        // Portfolio 수정사항 반영
        findPortfolio.editMe(request);

        portfolioFileRepository.updateStatusByIds(request.validFileIds(), Status.REGISTERED);
        portfolioFileRepository.updateStatusByIds(request.deletedFileIds(), Status.DELETING);
    }

    // 포트폴리오 삭제
    @Transactional
    public void removePortfolio(Long portfolioId, Member member) {
        // 삭제할 포트폴리오 Id로 찾기
        Portfolio findPortfolio = findPortfolioById(portfolioId);
        validatePortfolioAccess(member.getId(), findPortfolio);

        // 포트폴리오 삭제 (boolean type true로 변경)
        findPortfolio.removeMe();

        // 포트폴리오 삭제시 등록한 파일 및 이미지들도 완전 삭제
        List<PortfolioFile> portfolioFiles = findPortfolio.getPortfolioFiles();
        portfolioFiles.forEach(portfolioFile -> {
            // Image일 경우 thumbnaileh 삭제해 줘야 함
//            if (FileType.IMAGE == portfolioFile.getFileType()) {
//                awsS3Service.removeObject(portfolioFile.getThumbnailUrl());
//            }
            awsS3Service.removeObject(portfolioFile.getFileUrl());
        });
        portfolioFileRepository.deleteAllInBatch(portfolioFiles);
    }

    // 내가 등록한 모든 포트폴리오 가져오기 (구독 안 하면 최대 5개까지? 흠... 형편 없는 BM이구만...)
    public List<PortfolioResponse> findMyAllPortfolios(Member member) {
        return portfolioRepository.findAllWithFilesByOwnerIdAndRegisteredStatus(member.getId())
                .stream()
                .map(PortfolioResponse::from)
                .toList();
    }

    // 포트폴리오 단건 조회
    public PortfolioResponse findPortfolio(Long portfolioId) {
        Portfolio findPortfolio = findPortfolioById(portfolioId);
        return PortfolioResponse.from(findPortfolio);
    }


    private void validatePortfolioAccess(Long memberId, Portfolio portfolio) {
        if (portfolio.isMyPortfolio(memberId) == false) {
            throw new AccessDeniedException("포트폴리오에 접근할 권한이 없습니다");
        }
    }

    private Portfolio findPortfolioById(Long portfolioId) {
        Portfolio findPortfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new NotFoundException(PORTFOLIO_NOT_FOUND));

        findPortfolio.validateIsDeleted(); // 삭제된 포트폴리오인지 체크
        return findPortfolio;
    }
}
