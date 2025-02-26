package com.sparta.taptoon.domain.matchingpost.service;

import com.sparta.taptoon.domain.matchingpost.dto.request.AddMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.dto.request.RegisterMatchingPostRequest;
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
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
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

    // 빈 MatchingPost 만들기. 이미지 저장용
    @Transactional
    public Long generateEmptyMatchingPost(Long memberId) {
        Member findMember = findMemberById(memberId);
        MatchingPost savedEmtpyMatchingPost = matchingPostRepository.save(MatchingPost.builder()
                .title("")
                .author(findMember)
                .description("")
                .fileUrl("")
                .workType(WorkType.random())
                .artistType(ArtistType.random())
                .build());

        return savedEmtpyMatchingPost.getId();
    }

    // 매칭포스트 생성 (이건 이제 사용 안 됨)
    @Deprecated
    @Transactional
    public MatchingPostResponse makeNewMatchingPost(Long memberId, AddMatchingPostRequest request) {
        // Save to DB
        Member findMember = findMemberById(memberId);
        MatchingPost savedMatchingPost = matchingPostRepository.save(request.toEntity(findMember));

        // 2. 트랜잭션이 정상적으로 완료된 후 ES에 저장
        elasticMatchingPostManager.saveToElasticsearchAfterCommit(savedMatchingPost);

        return MatchingPostResponse.from(savedMatchingPost);
    }

    // 매칭 포스트 등록 (수정 작업을 하지만 사실상 등록 로직임)
    @Transactional
    public MatchingPostResponse registerMatchingPost(Long memberId, Long matchingPostId, RegisterMatchingPostRequest request) {
        MatchingPost findMatchingPost = findMatchingPostById(matchingPostId);
        if (findMatchingPost.isMyMatchingPost(memberId) == false) {
            throw new AccessDeniedException("매칭 게시글에 접근할 권한이 없습니다.");
        }

        findMatchingPost.registerMe(request);
        registerMatchingPostImages(request.matchingPostImageIds());

        elasticMatchingPostManager.upsertToElasticsearchAfterCommit(findMatchingPost);

        return MatchingPostResponse.from(findMatchingPost);
    }

    /*
        * MatchingPost 이미지 ID로 PENDING -> REGISTERED로 변경
        * `modifyMatchingPost()`의 트랜잭션 하에서 실행됨
     */
    private void registerMatchingPostImages(List<Long> matchingPostImageIds) {
        if (matchingPostImageIds.isEmpty()) return;

        List<MatchingPostImage> uploadedImages = matchingPostImageRepository.findAllById(matchingPostImageIds);
        if (uploadedImages.isEmpty() == false) {
            uploadedImages.forEach(MatchingPostImage::registerMe);
        }
    }

    // 매칭포스트 삭제 (soft deletion) 단, 사진이나 텍스트 파일을 어떻게 처리해야 할지 고려해야 함
    @Transactional
    public void removeMatchingPost(Long memberId, Long matchingPostId) {
        MatchingPost findMatchingPost = findMatchingPostById(matchingPostId);
        if (findMatchingPost.isMyMatchingPost(memberId) == false) {
            throw new AccessDeniedException("매칭 게시글에 접근할 권한이 없습니다");
        }

        // 삭제처리하면 게시글에 첨부된 이미지나 텍스트 파일을 어떻게 처리하지? 삭제해야 하나? -> 삭제해야 할 듯
        findMatchingPost.removeMe();

        // Delete from ES
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

    // Private helper method
    private MatchingPost findMatchingPostById(Long matchingPostId) {
        MatchingPost matchingPost = matchingPostRepository.findById(matchingPostId)
                .orElseThrow(() -> new NotFoundException(MATCHING_POST_NOT_FOUND));

        matchingPost.validateIsDeleted();

        return matchingPost;
    }

    // 임시 메서드 (나중에 교체, 삭제된 사용자인지도 사실 검증해야 함)
    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

}
