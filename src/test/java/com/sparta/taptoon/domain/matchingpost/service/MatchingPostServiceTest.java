package com.sparta.taptoon.domain.matchingpost.service;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostRepository;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
//@Transactional
@ActiveProfiles("dev")
@Tag("local-only") // 로컬에서만 실행되는 테스트 태그
class MatchingPostServiceTest {

    @Autowired
    MatchingPostRepository matchingPostRepository;

    @Autowired
    MatchingPostService matchingPostService;

    @Autowired
    MemberRepository memberRepository;

    MatchingPost matchingPost;
    Long initialViewCount;

    @BeforeEach
    void setup() {
        Member savedMember = memberRepository.save(Member.builder()
                .email("email")
                .name("name")
                .nickname("nickname")
                .password("password")
                .build());

        matchingPost = matchingPostRepository.save(MatchingPost.builder()
                .title("")
                .author(savedMember)
                .description("")
                .workType(WorkType.random())
                .artistType(ArtistType.random())
                .build());

        initialViewCount = matchingPost.getViewCount();
    }

    @Test
    void concurrentViewCountTest() throws InterruptedException {

        int threadCount = 10; // 동시 실행할 스레드 수
        int requestPerThread = 60; // 각 스레드당 호출 횟수
        int totalRequests = threadCount * requestPerThread; // 총 호출 횟수
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicLong finalViewCount = new AtomicLong(initialViewCount); // 동시 업데이트를 위한 AtomicLong

        // 각 스레드 실행
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < requestPerThread; j++) {
                        matchingPostService.findMatchingPostAndUpdateViews(matchingPost.getId());
                        finalViewCount.addAndGet(1); // 기대되는 viewCount 증가
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드가 완료될 때까지 대기
        latch.await();

        // executor 종료
        executorService.shutdown();

        // 데이터베이스에서 최종 viewCount 확인
        MatchingPost updatedMatchingPost = matchingPostRepository.findById(matchingPost.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATCHING_POST_NOT_FOUND));
        long actualViewCount = updatedMatchingPost.getViewCount();

        // 기대값과 실제값 비교
        assertEquals(initialViewCount + totalRequests, actualViewCount,
                "View count should increase by " + totalRequests + " but was " + (actualViewCount - initialViewCount));

        // AtomicLong으로 계산된 기대값과 비교 (동시성 문제 발생 여부 확인)
        assertEquals(finalViewCount.get(), actualViewCount,
                "AtomicLong tracked view count does not match database view count");
    }


}