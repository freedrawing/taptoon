package com.sparta.taptoon.domain.matchingpost;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostImageRepository;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostRepository;
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
public class CreateDummyData {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MatchingPostRepository matchingPostRepository;

    @Autowired
    MatchingPostImageRepository matchingPostImageRepository;

    // 스레드 풀의 크기를 CPU 코어 수에 맞게 설정
    private final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    // 한 번에 처리할 배치 크기를 설정합니다
    private final int BATCH_SIZE = 1000;

//    @Test
    void addDummyDataInMatchingPost() {

        Member member1 = memberRepository.save(createMember());
        Member member2 = memberRepository.save(createMember());

        // 스레드 풀을 생성
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 전체 작업을 배치 단위로 나눕니다
        int totalBatches = 500_000 / BATCH_SIZE;

        for (int batch = 0; batch < totalBatches; batch++) {
            // 각 배치를 비동기로 처리합니다
            CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
                processBatch(member1);
            }, executorService);

            CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
                processBatch(member2);
            }, executorService);

            futures.addAll(List.of(future1, future2));
        }

        // 모든 배치 작업이 완료될 때까지 기다립니다
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 스레드 풀을 정리합니다
        executorService.shutdown();
    }

    void processBatch(Member member) {
        // 배치 단위로 데이터를 처리합니다
        for (int i = 0; i < BATCH_SIZE; i++) {
            MatchingPost matchingPost = matchingPostRepository.save(createMatchingPost(member));

            for (int j = 0; j < 3; j++) {
                matchingPostImageRepository.save(createMatchingPostImage(matchingPost));
            }
        }
    }

}
