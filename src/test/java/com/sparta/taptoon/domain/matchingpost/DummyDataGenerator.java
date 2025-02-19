package com.sparta.taptoon.domain.matchingpost;

import com.sparta.taptoon.domain.matchingpost.dto.request.AddMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostImageRepository;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostRepository;
import com.sparta.taptoon.domain.matchingpost.service.MatchingPostService;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.sparta.taptoon.domain.util.EntityCreatorUtil.*;

@SpringBootTest
@ActiveProfiles("dev")
public class DummyDataGenerator {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MatchingPostRepository matchingPostRepository;

    @Autowired
    MatchingPostImageRepository matchingPostImageRepository;

    // 스레드 풀의 크기를 CPU 코어 수에 맞게 설정
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int BATCH_SIZE = 1000;
    private static final int TOTAL_RECORDS = 500_000;

    @Autowired
    private MatchingPostService matchingPostService;

    @Test
    void addDummyDataUsingService() {
        Member member1 = memberRepository.save(createMember());
        Member member2 = memberRepository.save(createMember());

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            int totalBatches = TOTAL_RECORDS / BATCH_SIZE;

            for (int batch = 0; batch < totalBatches; batch++) {
                List.of(
                        CompletableFuture.runAsync(() ->
                                processBatchUsingService(member1, createEnglishMatchingPostRequest()), executorService),
                        CompletableFuture.runAsync(() ->
                                processBatchUsingService(member2, createKoreanMatchingPostRequest()), executorService)
                ).forEach(futures::add);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executorService.shutdown(); // ExecutorService를 명시적으로 종료
        }
    }

    void processBatchUsingService(Member member, AddMatchingPostRequest request) {
        for (int i = 0; i < BATCH_SIZE; i++) {
            matchingPostService.makeNewMatchingPost(member.getId(), request);
        }
    }

//    @Test
    void addDummyDataUsingRepository() {
        Member member1 = memberRepository.save(createMember());
        Member member2 = memberRepository.save(createMember());

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            int totalBatches = TOTAL_RECORDS / BATCH_SIZE;

            for (int batch = 0; batch < totalBatches; batch++) {
                List.of(
                        CompletableFuture.runAsync(() -> processBatchUsingRepository(member1), executorService),
                        CompletableFuture.runAsync(() -> processBatchUsingRepository(member2), executorService)
                ).forEach(futures::add);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executorService.shutdown(); // ExecutorService를 명시적으로 종료
        }
    }

    void processBatchUsingRepository(Member member) {
        for (int i = 0; i < BATCH_SIZE; i++) {
            MatchingPost matchingPost = matchingPostRepository.save(createMatchingPost(member));
            for (int j = 0; j < 3; j++) {
                matchingPostImageRepository.save(createMatchingPostImage(matchingPost));
            }
        }
    }

}
