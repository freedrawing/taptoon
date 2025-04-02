package com.sparta.taptoon.domain.matchingpost.service;

import com.sparta.taptoon.domain.file.service.AwsS3Service;
import com.sparta.taptoon.domain.matchingpost.dto.request.RegisterMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.dto.request.UpdateMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostCursorResponse;
import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostResponse;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPostImage;
import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostImageRepository;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostRepository;
import com.sparta.taptoon.domain.matchingpost.repository.elastic.ElasticMatchingPostRepository;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.global.common.annotation.DistributedLock;
import com.sparta.taptoon.global.common.enums.Status;
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.sparta.taptoon.global.error.enums.ErrorCode.MATCHING_POST_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingPostService {

    private final MatchingPostRepository matchingPostRepository;
    private final MatchingPostImageRepository matchingPostImageRepository;
    private final ElasticMatchingPostRepository elasticMatchingPostRepository;
    private final ElasticMatchingPostManager elasticMatchingPostManager;
    private final ElasticAutocompleteService elasticAutocompleteService;
    private final AwsS3Service awsS3Service;

    @Transactional
    public Long generateEmptyMatchingPost(Member member) {
        MatchingPost savedEmtpyMatchingPost = matchingPostRepository.save(MatchingPost.builder()
                .title("")
                .author(member)
                .description("")
                .workType(WorkType.random())
                .artistType(ArtistType.random())
                .build());

        return savedEmtpyMatchingPost.getId();
    }

    @Transactional
    public Long generateEmptyMatchingPostImage(Long matchingPostId, String fileName, String thumbnailImageUrl, String originalImageUrl) {
        MatchingPost findMatchingPost = findMatchingPostById(matchingPostId);

        MatchingPostImage savedMatchingPostImage = matchingPostImageRepository.save(MatchingPostImage.builder()
                .matchingPost(findMatchingPost)
                .fileName(fileName)
                .thumbnailImageUrl(thumbnailImageUrl)
                .originalImageUrl(originalImageUrl)
                .build());

        return savedMatchingPostImage.getId();
    }

    @Transactional
    public MatchingPostResponse registerMatchingPost(Member member, Long matchingPostId, RegisterMatchingPostRequest request) {
        MatchingPost findMatchingPost = findMatchingPostById(matchingPostId);
        validateMatchingPostAccess(member.getId(), findMatchingPost);

        findMatchingPost.registerMe(request);
        registerMatchingPostImages(request.matchingPostImageIds());

        elasticMatchingPostManager.upsertToElasticsearchAfterCommit(findMatchingPost);

        return MatchingPostResponse.from(findMatchingPost);
    }

    public void registerMatchingPostImages(List<Long> matchingPostImageIds) {
        if (matchingPostImageIds.isEmpty()) return;

        List<MatchingPostImage> uploadedImages = matchingPostImageRepository.findAllById(matchingPostImageIds);
        if (uploadedImages.isEmpty() == false) {
            // 업로도된 이미지 REGISTERED 상태로 변경
            // 아래 방법으로 업데이트 하고 싶은데, 업데이트 과정에서 캐시에 반영이 안 돼서 귀찮음. 추후에 개선하자. 더티 체킹으로 업데이트하면 쿼리가 여러 번 나감
            // matchingPostImageRepository.updateStatusByIds(matchingPostImageIds, Status.REGISTERED);
            uploadedImages.forEach(MatchingPostImage::registerMe); // <- 이렇게 하면 간편한 건 맞지만 업데이트 쿼리가 여러번 나간다
        }

    }

    @Transactional
    public void editMatchingPost(Long memberId, Long matchingPostId, UpdateMatchingPostRequest request) {
        MatchingPost findMatchingPost = findMatchingPostById(matchingPostId);
        validateMatchingPostAccess(memberId, findMatchingPost);

        findMatchingPost.editMe(request);

        matchingPostImageRepository.updateStatusByIds(request.validImageIds(), Status.REGISTERED);
        matchingPostImageRepository.updateStatusByIds(request.deletedImageIds(), Status.DELETING);

        elasticMatchingPostManager.upsertToElasticsearchAfterCommit(findMatchingPost);
    }

    @Transactional
    public void removeMatchingPost(Member member, Long matchingPostId) {
        MatchingPost findMatchingPost = findMatchingPostById(matchingPostId);
        validateMatchingPostAccess(member.getId(), findMatchingPost);

        findMatchingPost.removeMe();

        List<MatchingPostImage> matchingPostImages = findMatchingPost.getMatchingPostImages();
        matchingPostImages.forEach(img -> {
            awsS3Service.removeObject(img.getThumbnailImageUrl());
            awsS3Service.removeObject(img.getOriginalImageUrl());
        });

        matchingPostImageRepository.deleteAllInBatch(matchingPostImages);

        elasticMatchingPostManager.deleteFromElasticsearchAfterCommit(matchingPostId);
    }

    public MatchingPostCursorResponse findFilteredMatchingPosts(
            String artistType,
            String workType,
            String keyword,
            Long lastViewCount,
            Long lastId,
            int pageSize) {

        elasticAutocompleteService.logSearchQuery(keyword);

        return elasticMatchingPostRepository.searchFrom(
                artistType,
                workType,
                keyword,
                lastViewCount,
                lastId,
                pageSize
        );
    }

    @DistributedLock(key = "#matchingPostId", waitTime = 10, leaseTime = 2)
    @Transactional
    public MatchingPostResponse findMatchingPostAndUpdateViews(Long matchingPostId) {
        MatchingPost findMatchingPost = findMatchingPostById(matchingPostId);
        findMatchingPost.increaseViewCount();

        elasticMatchingPostManager.upsertToElasticsearchAfterCommit(findMatchingPost);

        return MatchingPostResponse.from(findMatchingPost);
    }

    private MatchingPost findMatchingPostById(Long matchingPostId) {
        MatchingPost matchingPost = matchingPostRepository.findByIdWithAuthor(matchingPostId)
                .orElseThrow(() -> new NotFoundException(MATCHING_POST_NOT_FOUND));

        matchingPost.validateIsDeleted();

        return matchingPost;
    }

    private void validateMatchingPostAccess(Long memberId, MatchingPost matchingPost) {
        if (matchingPost.isMyMatchingPost(memberId) == false) {
            throw new AccessDeniedException("매칭 게시글에 접근할 권한이 없습니다");
        }
    }

}
