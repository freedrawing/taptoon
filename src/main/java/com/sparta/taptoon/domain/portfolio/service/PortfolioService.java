package com.sparta.taptoon.domain.portfolio.service;

import com.sparta.taptoon.domain.image.service.AwsS3Service;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.service.MemberService;
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
    private final MemberService memberService;


    private final int PORTFOLIO_LIMIT = 5;

    @Transactional
    public Long startPortfolio(Member member) {
        int countPortfolio = portfolioRepository.countPortfoliosByOwnerId(member.getId());

        if (PORTFOLIO_LIMIT <= countPortfolio) {
            throw new CreationLimitExceededException(CREATION_LIMIT_EXCEEDED);
        }

        Portfolio portfolio = Portfolio.builder()
                .owner(member)
                .title("")
                .content("")
                .build();
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        return savedPortfolio.getId();
    }

    @Transactional
    public Long generateEmptyPortfolioFile(Long portfolioId,
                                           String fileName,
                                           String fileType,
                                           String thumbnailImageUrl,
                                           String fileUrl) {

        Portfolio findPortfolio = findPortfolioById(portfolioId);

        PortfolioFile savedPortfolioFile = portfolioFileRepository.save(
                PortfolioFile.builder()
                        .portfolio(findPortfolio)
                        .fileName(fileName)
                        .thumbnailUrl(thumbnailImageUrl)
                        .fileUrl(fileUrl)
                        .fileType(fileType)
                        .build()
        );

        return savedPortfolioFile.getId();
    }


    @Transactional
    public PortfolioResponse registerPortfolio(RegisterPortfolioRequest request, Member member, Long portfolioId) {
        Portfolio findPortfolio = findPortfolioById(portfolioId);
        validatePortfolioAccess(member.getId(), findPortfolio);

        findPortfolio.registerPortfolio(request);
        registerPortfolioFiles(request.portfolioFileIds());

        return PortfolioResponse.from(findPortfolio);
    }

    private void registerPortfolioFiles(List<Long> portfolioFileIds) {
        if (portfolioFileIds.isEmpty()) return;

        List<PortfolioFile> uploadedPortfolioFiles = portfolioFileRepository.findAllById(portfolioFileIds);
        if (uploadedPortfolioFiles.isEmpty() == false) {
            portfolioFileRepository.updateStatusByIds(portfolioFileIds, Status.REGISTERED);
        }
    }

    // 포트폴리오 진짜 수정
    @Transactional
    public void editPortfolio(Long portfolioId, Member member, UpdatePortfolioRequest request) {
        Portfolio findPortfolio = findPortfolioById(portfolioId);
        validatePortfolioAccess(member.getId(), findPortfolio);

        findPortfolio.editMe(request);

        portfolioFileRepository.updateStatusByIds(request.validFileIds(), Status.REGISTERED);
        portfolioFileRepository.updateStatusByIds(request.deletedFileIds(), Status.DELETING);
    }

    @Transactional
    public void removePortfolio(Long portfolioId, Member member) {
        Portfolio findPortfolio = findPortfolioById(portfolioId);
        validatePortfolioAccess(member.getId(), findPortfolio);

        findPortfolio.removeMe();

        List<PortfolioFile> portfolioFiles = findPortfolio.getPortfolioFiles();
        portfolioFiles.forEach(portfolioFile -> {
            if (FileType.IMAGE == portfolioFile.getFileType()) {
                awsS3Service.removeObject(portfolioFile.getThumbnailUrl());
            }
            awsS3Service.removeObject(portfolioFile.getFileUrl());
        });
        portfolioFileRepository.deleteAllInBatch(portfolioFiles);
    }

    public List<PortfolioResponse> findAllPortfoliosBy(Long memberId) {

        Member findMember = memberService.findMemberById(memberId);

        return portfolioRepository.findAllWithFilesByOwnerIdAndRegisteredStatus(findMember.getId())
                .stream()
                .map(PortfolioResponse::from)
                .toList();
    }

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

        findPortfolio.validateIsDeleted();
        return findPortfolio;
    }
}
