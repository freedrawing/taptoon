package com.sparta.taptoon.domain.matchingpost.service;

import com.sparta.taptoon.domain.matchingpost.entity.document.AutocompleteDocument;
import com.sparta.taptoon.domain.matchingpost.repository.elastic.ElasticAutocompleteRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
@ActiveProfiles("dev") // 테스트용 프로파일 사용
@Tag("local-only") // 로컬에서만 실행되는 테스트 태그
public class ElasticAutocompleteServiceConcurrentTest {

    @Autowired
    private ElasticAutocompleteService elasticAutocompleteService;

    @Autowired
    private ElasticAutocompleteRepository elasticAutocompleteRepository;

    private static final int THREAD_COUNT = 40; // 동시 스레드 수
    private static final String TEST_KEYWORD = "testKeyword";
    private static final int EXPECTED_INCREASE = THREAD_COUNT; // 예상 증가 횟수

    @BeforeEach
    void setUp() {
        // 테스트 전 데이터 초기화
        elasticAutocompleteRepository.deleteById(TEST_KEYWORD); // 기존 데이터 삭제
        AutocompleteDocument initialDoc = new AutocompleteDocument(TEST_KEYWORD);
        elasticAutocompleteRepository.save(initialDoc);
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 데이터 정리
        elasticAutocompleteRepository.deleteById(TEST_KEYWORD);
    }

    @Test
    @DisplayName("동시성 테스트: @DistributedLock이 searchCount 증가를 안전하게 보장")
    void elasticConcurrentTest() throws InterruptedException, ExecutionException {
        // 초기 searchCount 확인
        AutocompleteDocument initialDoc = elasticAutocompleteRepository.findById(TEST_KEYWORD)
                .orElseThrow(() -> new RuntimeException("초기 문서가 없습니다."));
        long initialCount = initialDoc.getSearchCount();
        log.info("초기 searchCount: {}", initialCount);

        // ExecutorService로 다중 스레드 생성
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT); // 모든 스레드가 완료될 때까지 대기
        CompletableFuture<?>[] futures = new CompletableFuture[THREAD_COUNT];

        // 다중 스레드에서 logSearchQuery 호출
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    log.info("스레드 {}: logSearchQuery 시작", threadId);
                    elasticAutocompleteService.logSearchQuery(TEST_KEYWORD);
                    log.info("스레드 {}: logSearchQuery 완료", threadId);
                } catch (Exception e) {
                    log.error("스레드 {}에서 예외 발생: {}", threadId, e.getMessage());
                } finally {
                    latch.countDown(); // 스레드 완료 시 카운트 감소
                }
            }, executorService);
        }

        // 모든 스레드가 완료될 때까지 대기
        latch.await(10, TimeUnit.SECONDS); // 10초 타임아웃
        executorService.shutdown();

        // 최종 searchCount 확인
        AutocompleteDocument finalDoc = elasticAutocompleteRepository.findById(TEST_KEYWORD)
                .orElseThrow(() -> new RuntimeException("최종 문서가 없습니다."));
        long finalCount = finalDoc.getSearchCount();
        log.info("최종 searchCount: {}", finalCount);

        // 검증: 초기값 + 호출 횟수와 일치해야 함
        assertEquals(initialCount + EXPECTED_INCREASE, finalCount,
                "searchCount가 예상대로 증가하지 않았습니다. 예상: " + (initialCount + EXPECTED_INCREASE) + ", 실제: " + finalCount);
    }
}