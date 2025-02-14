package com.sparta.taptoon.domain.matchingpost.service;

import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostResponse;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostRepository;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import com.sparta.taptoon.global.error.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.sparta.taptoon.global.error.enums.ErrorCode.MATCHING_POST_NOT_FOUND;

// Lock별 성능 비교를 위한 클래스. 실제 서비스로직에는 사용X
@Deprecated
@Service
@RequiredArgsConstructor
public class DeprecatedMatchingPostService {

    private final MatchingPostService matchingPostService;
    private final MatchingPostRepository matchingPostRepository;
    private final RedissonClient redissonClient;

    // 매칭 포스트 단건 조회 + 조회수 증가 (Pessimistic Lock)
    @Transactional
    public MatchingPostResponse findMatchingPostV1WithLock(Long matchingPostId) {
        MatchingPost findMatchingPost = matchingPostRepository.findByIdWithLock(matchingPostId)
                .orElseThrow(() -> new NotFoundException(MATCHING_POST_NOT_FOUND));

        findMatchingPost.increaseViewCount();

        return MatchingPostResponse.from(findMatchingPost);
    }

    // 여기는 Transaction 추가하면 안 됨. `lock`이 `@Transactional`보다 스코프가 길어야 함
    public MatchingPostResponse findMatchingPostV2UsingRedisson(Long matchingPostId) {
        String className = this.getClass().getSimpleName();
        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        String key = String.format("%s.%s:%s", className, methodName, matchingPostId);

        RLock lock = redissonClient.getLock(key);
        boolean locked = false;

        try {
            //  락을 시도하는데, 최대 OO(분, 시간)까지 대기하고, 락을 획득하면 OO초(분, 시간) 후에는 자동 해제 되도록 설정
            locked = lock.tryLock(10, 2, TimeUnit.SECONDS);

            if (locked) {
                return matchingPostService.findMatchingPostAndUpdateViewsV2(matchingPostId);
            } else {
                throw new TooManyRequestsException();
            }
        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
            throw new TooManyRequestsException();
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
