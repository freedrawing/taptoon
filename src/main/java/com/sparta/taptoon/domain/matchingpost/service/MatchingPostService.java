package com.sparta.taptoon.domain.matchingpost.service;

import com.sparta.taptoon.domain.matchingpost.dto.request.AddMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.dto.request.UpdateMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostResponse;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostRepository;
import com.sparta.taptoon.domain.user.entity.User;
import com.sparta.taptoon.domain.user.repository.UserRepository;
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
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
    private final UserRepository userRepository; // 나중에 서비스로 바꿔야 함.

    @Transactional
    public MatchingPostResponse makeNewMatchingPost(Long userId, AddMatchingPostRequest request) {
        User findUser = findUserById(userId);
        MatchingPost savedMatchingPost = matchingPostRepository.save(request.toEntity(findUser));

        return MatchingPostResponse.from(savedMatchingPost);
    }

    @Transactional
    public void removeMatchingPost(Long userId, Long matchingPostId) {
        MatchingPost findMatchingPost = findMatchingPostById(matchingPostId);
        if (findMatchingPost.isMyMatchingPost(userId)) {
            throw new AccessDeniedException("매칭 게시글에 접근할 권한이 없습니다");
        }

        // 삭제처리하면 게시글에 첨부된 이미지나 텍스트 파일을 어떻게 처리하지? 삭제해야 하나? -> 삭제해야 할 듯
        findMatchingPost.removeMe();
    }

    @Transactional
    public MatchingPostResponse modifyMatchingPost(Long userId, Long matchingPostId, UpdateMatchingPostRequest request) {
        MatchingPost findMatchingPost = findMatchingPostById(matchingPostId);
        if (findMatchingPost.isMyMatchingPost(userId)) {
            throw new AccessDeniedException("매칭 게시글에 접근할 권한이 없습니다.");
        }

        // 파일이랑 기타 정보 모두 수정해야 할 듯
        findMatchingPost.modifyMe(request);

        return MatchingPostResponse.from(findMatchingPost);
    }

    public Page<MatchingPostResponse> findFilteredMatchingPostResponse() {
        return null;
    }

    // 특정 매칭 게시글 조회 + 조회수 1증가 (동시성 처리해야 함)
    @Transactional
    public MatchingPostResponse findMatchingPostAndIncreaseViewCount(Long matchingPostId) {
        MatchingPost findMatching = matchingPostRepository.findByIdWithLock(matchingPostId)
                .orElseThrow(() -> new NotFoundException(MATCHING_POST_NOT_FOUND));
        findMatching.increaseViewCount();

        return MatchingPostResponse.from(findMatching);
    }

    private MatchingPost findMatchingPostById(Long matchingPostId) {
        return matchingPostRepository.findById(matchingPostId)
                .orElseThrow(() -> new NotFoundException(MATCHING_POST_NOT_FOUND));
    }

    // 임시 메서드 (나중에 교체)
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
    }


}
