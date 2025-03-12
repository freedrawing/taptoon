package com.sparta.taptoon.domain.matchingpost.service;

import com.sparta.taptoon.domain.image.service.AwsS3Service;
import com.sparta.taptoon.domain.image.service.ImageService;
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
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.common.annotation.DistributedLock;
import com.sparta.taptoon.global.common.enums.Status;
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.sparta.taptoon.global.error.enums.ErrorCode.MATCHING_POST_NOT_FOUND;
import static com.sparta.taptoon.global.error.enums.ErrorCode.MEMBER_NOT_FOUND;

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
    private final MemberRepository memberRepository; // 나중에 서비스로 바꿔야 함.
    private final AwsS3Service awsS3Service;

    // 빈 MatchingPost 만들기. 이미지 저장용
    @Transactional
    public Long generateEmptyMatchingPost(Long memberId) {
        Member findMember = findMemberById(memberId);
        MatchingPost savedEmtpyMatchingPost = matchingPostRepository.save(MatchingPost.builder()
                .title("")
                .author(findMember)
                .description("")
                .workType(WorkType.random())
                .artistType(ArtistType.random())
                .build());

        return savedEmtpyMatchingPost.getId();
    }

    // 빈 MatchingPostImage 만들기. 이미지 저장용 (저장경로 예시: https://taptoon.s3.ap-northeast-2.amazonaws.com/matchingpost/original/117-taptoon-logo-circular.png)
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

    // 매칭 포스트 등록 (수정 작업을 하지만 사실상 등록 로직임)
    @Transactional
    public MatchingPostResponse registerMatchingPost(Long memberId, Long matchingPostId, RegisterMatchingPostRequest request) {
        MatchingPost findMatchingPost = findMatchingPostById(matchingPostId);
        validateMatchingPostAccess(memberId, findMatchingPost);

        // `MatchingPost` & `MatchingPostImage` 등록
        findMatchingPost.registerMe(request);
        registerMatchingPostImages(request.matchingPostImageIds());

        elasticMatchingPostManager.upsertToElasticsearchAfterCommit(findMatchingPost);

        return MatchingPostResponse.from(findMatchingPost);
    }

    /*
     * MatchingPost 이미지 ID로 PENDING -> REGISTERED로 변경
     * `registerMatchingPost()`의 트랜잭션 하에서 실행됨
     */
//    @Transactional
    public void registerMatchingPostImages(List<Long> matchingPostImageIds) {
        if (matchingPostImageIds.isEmpty()) return;

        List<MatchingPostImage> uploadedImages = matchingPostImageRepository.findAllById(matchingPostImageIds);
        if (uploadedImages.isEmpty() == false) {
            // 업로도된 이미지 REGISTERED 상태로 변경
            // 아래 방법으로 업데이트 하고 싶은데, 업데이트 과정에서 캐시에 반영이 안 돼서 귀찮음. 추후에 개선하자. 더티 체킹으로 업데이트하면 쿼리가 여러 번 나감
//            matchingPostImageRepository.updateStatusByIds(matchingPostImageIds, Status.REGISTERED);
            uploadedImages.forEach(MatchingPostImage::registerMe); // <- 이렇게 하면 간편한 건 맞지만 업데이트 쿼리가 여러번 나간다
        }

    }

    // MatchingPost 수정 + 이미지 삭제된 것과 추가된 것 각각 상태 변경
    @Transactional
    public void editMatchingPost(Long memberId, Long matchingPostId, UpdateMatchingPostRequest request) {
        // 기존에 연결됐던 이미지 중, update시 ID 값이 안 넘어오면 전부 DELETING으로 바꾸자.
        // PENDING 상태가 몇 시간 이상이 유지되면 DELETING으로 상태를 변경하고, DELETING은 Scheduler 같은 녀석이 1시간마다 삭제하는 걸로
        MatchingPost findMatchingPost = findMatchingPostById(matchingPostId);
        validateMatchingPostAccess(memberId, findMatchingPost);

        // MatchingPost 수정사항 반영
        findMatchingPost.editMe(request);

        // 이미지 추가된 것과 삭제된 것 상태 변경
        matchingPostImageRepository.updateStatusByIds(request.validImageIds(), Status.REGISTERED);
        matchingPostImageRepository.updateStatusByIds(request.deletedImageIds(), Status.DELETING);

        // Update Elasticsearch
        // (사실 아래 Elasticsearch 최신화 하는 것도, 처음에 Elasticsearch에는 Image 데이터가 안 들어가는데, 수정 후 바로 상세페이지로 이동해서 최신화가 되는 거임)
        elasticMatchingPostManager.upsertToElasticsearchAfterCommit(findMatchingPost);
    }

    // 매칭포스트 삭제 (soft deletion) 단, 사진이나 텍스트 파일을 어떻게 처리해야 할지 고려해야 함
    @Transactional
    public void removeMatchingPost(Long memberId, Long matchingPostId) {
        MatchingPost findMatchingPost = findMatchingPostById(matchingPostId);
        validateMatchingPostAccess(memberId, findMatchingPost);

        findMatchingPost.removeMe();

        // S3 이미지 삭제 후 컬렉션 비우기
        List<MatchingPostImage> matchingPostImages = findMatchingPost.getMatchingPostImages();
        matchingPostImages.forEach(img -> {
//            awsS3Service.removeObject(img.getThumbnailImageUrl()); // delete thumbnail // 그런데 썸네일은 완벽히 저장 로직이 갖춰지지 않은 듯
            awsS3Service.removeObject(img.getOriginalImageUrl()); // delete original
        }); // 나중에 thumbnail도 삭제해줘야 함

        // 매칭포스트와 함께 등록된 모든 이미지 삭제
        matchingPostImageRepository.deleteAllInBatch(matchingPostImages);

        // Delete from Elasticsearch
        elasticMatchingPostManager.deleteFromElasticsearchAfterCommit(matchingPostId);
    }

    /*
     * 매칭 포스트 필터링 다건 검색 (using Elasticsearch)
     * 예외 처리를 해야 할 것 같지만 검색 중 예외가 발생하면 사실 Elasticsearch 내부에서 발생한 예외일 것이므로,
     * 그때는 500번대 에러가 맞는 듯하다. 굳이 할 필요 없을 수도?
     */
    public MatchingPostCursorResponse findFilteredMatchingPosts(
            String artistType,
            String workType,
            String keyword,
            Long lastViewCount,
            Long lastId,
            int pageSize) {

        // 검색어 인덱싱
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

    // 여기서 ES에 있는 값까지 업데이트 해주는 건 다소 비효율적인데 나중에 바꿔야 할 듯
    // 매칭 포스트 단건 조회 + 조회수 증가 (update 로직을 분리하고 싶은데, AOP라서 분리하기가 다소 번거롭다)
    @DistributedLock(key = "#matchingPostId", waitTime = 10, leaseTime = 2)
    @Transactional
    public MatchingPostResponse findMatchingPostAndUpdateViews(Long matchingPostId) {
        MatchingPost findMatchingPost = findMatchingPostById(matchingPostId);
        findMatchingPost.increaseViewCount();

        // 조회수 업데이트 (다만 이건 다소 비효율적이다)
        elasticMatchingPostManager.upsertToElasticsearchAfterCommit(findMatchingPost);

        return MatchingPostResponse.from(findMatchingPost);
    }

    // Find valid MatchingPost
    private MatchingPost findMatchingPostById(Long matchingPostId) {
        MatchingPost matchingPost = matchingPostRepository.findById(matchingPostId)
                .orElseThrow(() -> new NotFoundException(MATCHING_POST_NOT_FOUND));

        matchingPost.validateIsDeleted();

        return matchingPost;
    }

    // MatchingPost 유효성 검사
    private void validateMatchingPostAccess(Long memberId, MatchingPost matchingPost) {
        if (matchingPost.isMyMatchingPost(memberId) == false) {
            throw new AccessDeniedException("매칭 게시글에 접근할 권한이 없습니다");
        }
    }

    // 임시 메서드 (나중에 교체, 삭제된 사용자인지도 사실 검증해야 함)
    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

}
