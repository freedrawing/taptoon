package com.sparta.taptoon.domain.matchingpost.service;

import com.sparta.taptoon.domain.matchingpost.dto.request.AddMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.dto.request.UpdateMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostResponse;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostRepository;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.common.annotation.DistributedLock;
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.sparta.taptoon.global.error.enums.ErrorCode.MATCHING_POST_NOT_FOUND;
import static com.sparta.taptoon.global.error.enums.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingPostService {

    private final MatchingPostRepository matchingPostRepository;
    private final MemberRepository memberRepository; // 나중에 서비스로 바꿔야 함.

    // 매칭포스트 생성
    @Transactional
    public MatchingPostResponse makeNewMatchingPost(Long userId, AddMatchingPostRequest request) {
        Member findMember = findMemberById(userId);
        MatchingPost savedMatchingPost = matchingPostRepository.save(request.toEntity(findMember));

        return MatchingPostResponse.from(savedMatchingPost);
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

        return MatchingPostResponse.from(findMatchingPost);
    }

    // 매칭 포스트 필터링 다건 검색
    public Page<MatchingPostResponse> findFilteredMatchingPosts(String artistType, String workType, String keyword) {
        // TODO: 필터링 로직 구현 (인덱스 비교한 후 실행)
        return Page.empty();
    }

    // 매칭 포스트 단건 조회 + 조회수 증가 (update 로직을 분리하고 싶은데, AOP라서 분리하기가 다소 번거롭다)
    @DistributedLock(key = "#matchingPostId", waitTime = 10, leaseTime = 2)
    @Transactional
    public MatchingPostResponse findMatchingPostAndUpdateViewsV3(Long matchingPostId) {
        MatchingPost findMatchingPost = matchingPostRepository.findById(matchingPostId)
                .orElseThrow(() -> new NotFoundException(MATCHING_POST_NOT_FOUND));

        findMatchingPost.increaseViewCount();

        return MatchingPostResponse.from(findMatchingPost);
    }

    // Redisson V2 Lock을 위한 테스트용 (트랜잭션 범위 안 맞아서..)
    @Deprecated
    @Transactional
    public MatchingPostResponse findMatchingPostAndUpdateViewsV2(Long matchingPostId) {
        MatchingPost findMatchingPost = matchingPostRepository.findById(matchingPostId)
                .orElseThrow(() -> new NotFoundException(MATCHING_POST_NOT_FOUND));

        findMatchingPost.increaseViewCount();

        return MatchingPostResponse.from(findMatchingPost);
    }

    // Private helper method
    private MatchingPost findMatchingPostById(Long matchingPostId) {
        return matchingPostRepository.findById(matchingPostId)
                .orElseThrow(() -> new NotFoundException(MATCHING_POST_NOT_FOUND));
    }

    // 임시 메서드 (나중에 교체)
    private Member findMemberById(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
    }


}
