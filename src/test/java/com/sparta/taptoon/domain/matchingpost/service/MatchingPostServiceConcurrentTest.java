package com.sparta.taptoon.domain.matchingpost.service;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostRepository;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static com.sparta.taptoon.domain.util.EntityCreatorUtil.createMatchingPost;
import static com.sparta.taptoon.domain.util.EntityCreatorUtil.createMember;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
@ActiveProfiles("suk-test")
class MatchingPostServiceConcurrentTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MatchingPostService matchingPostService;

    @Autowired
    MatchingPostRepository matchingPostRepository;

    @Autowired
    MatchingPostTestService matchingPostTestService;

//    @AfterEach
//    void afterEach() {
//        matchingPostRepository.deleteAllInBatch();
//        memberRepository.deleteAllInBatch();
//    }

    // 락에 대한 범위 때문에 속도
    @Test
    void 조회수_동시성_문제_테스트_With_Pessimistic_LOCK() throws InterruptedException {
        executeConcurrentTest(matchingPostTestService::findMatchingPostV1WithLock, 10, 1_000);
    }

//    @Test
    void 조회수_동시성_문제_테스트_With_Redisson() throws InterruptedException {
        executeConcurrentTest(matchingPostTestService::findMatchingPostV2UsingRedisson, 3, 1_000);
    }

    @Test
    void 조회수_동시성_문제_테스트_With_Redisson_Annotation() throws InterruptedException {
        executeConcurrentTest(matchingPostService::findMatchingPostAndUpdateViewsV3, 10, 1_000);
    }

    // 동시성 테스트 실행 메서드 (공통 로직)
    private void executeConcurrentTest(Consumer<Long> testFunction, int threadCount, int numberOfRequests) throws InterruptedException {
        // given
        Member member = memberRepository.save(createMember());
        MatchingPost matchingPost = matchingPostRepository.save(createMatchingPost(member));

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < numberOfRequests; j++) {
                        testFunction.accept(matchingPost.getId());
                        log.info("threadName={}", Thread.currentThread().getName());
//                        MatchingPost updatedPost = matchingPostRepository.findById(matchingPost.getId()).orElseThrow();
//                        log.info("(Thread-{}) viewCount={}", Thread.currentThread().getName(), updatedPost.getViewCount());
                    }
                } catch (Exception e) {
                    log.error("조회수 증가 실패", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        MatchingPost result = matchingPostRepository.findById(matchingPost.getId()).orElseThrow();
        log.info("Final viewCount={}", result.getViewCount());
//        assertThat(numberOfRequests * threadCount).isEqualTo(result.getViewCount());
        assertEquals(numberOfRequests * threadCount, result.getViewCount());
    }
}