package com.sparta.taptoon.domain.matchingpost.service;

import com.sparta.taptoon.domain.matchingpost.dto.request.AddMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.dto.request.UpdateMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostResponse;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostRepository;
import com.sparta.taptoon.domain.matchingpost.repository.elasticsearch.ElasticMatchingPostRepository;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.common.annotation.DistributedLock;
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.sparta.taptoon.global.error.enums.ErrorCode.MATCHING_POST_NOT_FOUND;
import static com.sparta.taptoon.global.error.enums.ErrorCode.USER_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingPostService {

    private final MatchingPostRepository matchingPostRepository;
    private final ElasticMatchingPostRepository elasticMatchingPostRepository;
    private final ElasticMatchingPostManager elasticMatchingPostManager;
    private final MemberRepository memberRepository; // 나중에 서비스로 바꿔야 함.

    // 매칭포스트 생성
    @Transactional
    public MatchingPostResponse makeNewMatchingPost(Long mermberId, AddMatchingPostRequest request) {
        // Save to DB
        Member findMember = findMemberById(mermberId);
        MatchingPost savedMatchingPost = matchingPostRepository.save(request.toEntity(findMember));

        // 2. 트랜잭션이 정상적으로 완료된 후 ES에 저장
        elasticMatchingPostManager.saveToESAfterCommit(savedMatchingPost);

        return MatchingPostResponse.from(savedMatchingPost);
    }

    // 매칭 포스트 수정(일괄 수정)
    @Transactional
    public MatchingPostResponse modifyMatchingPost(Long userId, Long matchingPostId, UpdateMatchingPostRequest request) {
        MatchingPost findMatchingPost = findMatchingPostById(matchingPostId);
        if (findMatchingPost.isMyMatchingPost(userId) == false) {
            throw new AccessDeniedException("매칭 게시글에 접근할 권한이 없습니다.");
        }

        // 파일이랑 기타 정보 모두 수정해야 할 듯
        findMatchingPost.modifyMe(request);

        // Upsert to ES
        elasticMatchingPostManager.upsertToESAfterCommit(findMatchingPost);

        return MatchingPostResponse.from(findMatchingPost);
    }

    // 매칭포스트 삭제 (soft deletion) 단, 사진이나 텍스트 파일을 어떻게 처리해야 할지 고려해야 함
    @Transactional
    public void removeMatchingPost(Long userId, Long matchingPostId) {
        MatchingPost findMatchingPost = findMatchingPostById(matchingPostId);
        if (findMatchingPost.isMyMatchingPost(userId) == false) {
            throw new AccessDeniedException("매칭 게시글에 접근할 권한이 없습니다");
        }

        // 삭제처리하면 게시글에 첨부된 이미지나 텍스트 파일을 어떻게 처리하지? 삭제해야 하나? -> 삭제해야 할 듯
        findMatchingPost.removeMe();

        // Delete from ES
        elasticMatchingPostManager.deleteFromESAfterCommit(matchingPostId);
    }

    // 매칭 포스트 필터링 다건 검색
    public Page<MatchingPostResponse> findFilteredMatchingPosts(String artistType, String workType, String keyword, Pageable pageable) {

        List<Long> matchingIds = elasticMatchingPostRepository.searchIdsByKeyword(keyword);

        return matchingPostRepository.searchMatchingPostsFromCondition(
//                ArtistType.fromString(artistType),
//                WorkType.fromString(workType),
                null,
                null,
                matchingIds,
                pageable
        );
    }

    // 매칭 포스트 단건 조회 + 조회수 증가 (update 로직을 분리하고 싶은데, AOP라서 분리하기가 다소 번거롭다)
    @DistributedLock(key = "#matchingPostId", waitTime = 10, leaseTime = 2)
    @Transactional
    public MatchingPostResponse findMatchingPostAndUpdateViewsV3(Long matchingPostId) {
        MatchingPost findMatchingPost = findMatchingPostById(matchingPostId);
        findMatchingPost.increaseViewCount();

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
    private Member findMemberById(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
    }


}
