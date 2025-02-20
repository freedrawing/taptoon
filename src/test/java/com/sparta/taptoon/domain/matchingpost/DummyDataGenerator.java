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
import java.util.Arrays;
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
    private static final int TOTAL_RECORDS = 2_000;

    @Autowired
    private MatchingPostService matchingPostService;

//    @Test
    void dummy() {
        List<AddMatchingPostRequest> dummyRequests = Arrays.asList(
                new AddMatchingPostRequest("웹툰 작가를 찾고 있습니다!", "illustrator", "hybrid", "판타지 장르의 웹툰을 준비 중입니다. 함께 성장할 수 있는 분을 찾습니다."),
                new AddMatchingPostRequest("시나리오 작가님 구합니다", "writer", "online", "로맨스 웹툰 연재 예정입니다. 경험 있는 작가님 환영합니다."),
                new AddMatchingPostRequest("만화 작가 구인", "writer", "offline", "액션 장르 웹툰 프로젝트입니다. 서울 지역 거주자 우대합니다."),
                new AddMatchingPostRequest("웹툰 파트너십 제안", "illustrator", "online", "SF 웹툰 연재를 준비 중입니다. 포트폴리오 보유자 우대합니다."),
                new AddMatchingPostRequest("장르 웹툰 작가님 모십니다", "writer", "hybrid", "미스터리 장르 웹툰을 함께 만들어갈 분을 찾습니다."),
                new AddMatchingPostRequest("웹툰 협업 파트너 구함", "illustrator", "offline", "일상 로맨스 웹툰을 준비 중입니다. 정기적인 오프라인 미팅 가능하신 분."),
                new AddMatchingPostRequest("웹툰 시나리오 작가 구인", "writer", "online", "판타지 로맨스 웹툰입니다. 주 2회 연재 예정입니다."),
                new AddMatchingPostRequest("웹툰 일러스트레이터 구합니다", "illustrator", "hybrid", "개그 웹툰 프로젝트입니다. 캐릭터 디자인에 자신 있는 분."),
                new AddMatchingPostRequest("웹툰 작가 파트너십", "writer", "offline", "스릴러 장르 웹툰입니다. 서울/경기 지역 거주자 우대."),
                new AddMatchingPostRequest("함께 웹툰 만들어요", "illustrator", "online", "힐링 장르 웹툰을 계획 중입니다. 따뜻한 그림체 선호."),
                new AddMatchingPostRequest("웹툰 공동 작업자 모집", "writer", "hybrid", "학원물 웹툰 준비 중입니다. 플랫폼 연재 경험자 우대."),
                new AddMatchingPostRequest("웹툰 작가님 구인합니다", "illustrator", "offline", "스포츠 장르 웹툰입니다. 동적인 그림체에 자신 있는 분."),
                new AddMatchingPostRequest("시나리오 작가 구합니다", "writer", "online", "로맨스 판타지 웹툰입니다. 주 3회 연재 목표."),
                new AddMatchingPostRequest("웹툰 일러스트레이터 찾습니다", "illustrator", "hybrid", "무협 장르입니다. 액션신 묘사에 능숙하신 분."),
                new AddMatchingPostRequest("웹툰 작가 구인", "writer", "offline", "공포 스릴러 장르입니다. 정기적인 미팅 가능하신 분."),
                new AddMatchingPostRequest("웹툰 프로젝트 멤버 모집", "illustrator", "online", "청춘 로맨스 웹툰입니다. 밝은 그림체 선호."),
                new AddMatchingPostRequest("웹툰 작가님을 찾습니다", "writer", "hybrid", "역사 판타지 장르입니다. 사전 자료 조사 가능하신 분."),
                new AddMatchingPostRequest("일러스트레이터 구인", "illustrator", "offline", "일상 개그물입니다. 귀여운 그림체 선호."),
                new AddMatchingPostRequest("웹툰 시나리오 작가 모집", "writer", "online", "SF 장르입니다. 세계관 구축에 관심 있는 분."),
                new AddMatchingPostRequest("웹툰 작화 담당자 구합니다", "illustrator", "hybrid", "미스터리 추리물입니다. 섬세한 연출 가능하신 분."),
                new AddMatchingPostRequest("웹툰 팀원 모집", "illustrator", "online", "로맨틱 코미디 장르입니다. 따뜻한 감성의 작화를 원합니다."),
                new AddMatchingPostRequest("웹툰 공동 창작자 찾습니다", "writer", "hybrid", "드라마 장르입니다. 깊이 있는 스토리 전개를 함께 고민하실 분."),
                new AddMatchingPostRequest("웹툰 제작팀을 꾸립니다", "illustrator", "offline", "공포 웹툰입니다. 어두운 분위기 연출 가능하신 분."),
                new AddMatchingPostRequest("함께 웹툰을 만들어가실 분", "writer", "online", "미스터리 장르의 웹툰입니다. 복선과 반전을 잘 살릴 수 있는 분."),
                new AddMatchingPostRequest("웹툰 작가 및 일러스트레이터 구인", "illustrator", "hybrid", "무협 판타지 장르입니다. 액션 장면에 강한 분."),
                new AddMatchingPostRequest("웹툰 프로젝트 멤버 찾습니다", "writer", "offline", "스릴러 장르 웹툰을 함께 기획하실 분."),
                new AddMatchingPostRequest("웹툰 작화가 모집", "illustrator", "online", "힐링물입니다. 따뜻한 색감과 분위기 연출 가능하신 분."),
                new AddMatchingPostRequest("웹툰 글 작가 구합니다", "writer", "hybrid", "역사 판타지 장르입니다. 고증에 신경 쓸 수 있는 분."),
                new AddMatchingPostRequest("웹툰 협업하실 분 구합니다", "illustrator", "offline", "판타지 모험물입니다. 배경 작화 가능자 우대."),
                new AddMatchingPostRequest("웹툰 창작팀 모집", "writer", "online", "로맨스 코미디 장르입니다. 감성적인 대사를 잘 쓰는 분."),
                new AddMatchingPostRequest("웹툰 팀을 꾸리고 있습니다", "illustrator", "hybrid", "공포 스릴러 장르입니다. 무서운 분위기 연출 가능하신 분."),
                new AddMatchingPostRequest("웹툰 원고 작업 도와주실 분", "writer", "offline", "드라마 웹툰입니다. 현실적인 스토리 진행을 선호하는 분."),
                new AddMatchingPostRequest("웹툰 스토리 작가 구인", "illustrator", "online", "로맨틱 판타지 장르입니다. 여성향 그림체 선호."),
                new AddMatchingPostRequest("웹툰 작가 및 그림작가 모집", "writer", "hybrid", "액션 장르 웹툰입니다. 긴장감 있는 연출 가능하신 분."),
                new AddMatchingPostRequest("웹툰 제작 인력 구합니다", "illustrator", "offline", "스포츠 장르 웹툰입니다. 움직임 표현이 뛰어난 분."),
                new AddMatchingPostRequest("웹툰 시나리오 작가 및 작화가 모집", "writer", "online", "판타지 장르입니다. 대사와 서사가 자연스러운 분."),
                new AddMatchingPostRequest("웹툰 공동 제작자 찾습니다", "illustrator", "hybrid", "일상 개그물입니다. 유머 코드가 뛰어난 분."),
                new AddMatchingPostRequest("웹툰 창작팀에서 작가님을 모십니다", "writer", "offline", "드라마 장르 웹툰입니다. 감정선이 섬세한 스토리를 선호하는 분."),
                new AddMatchingPostRequest("웹툰 그림 작가를 모집합니다", "illustrator", "online", "추리물 웹툰입니다. 디테일한 장면 연출 가능하신 분."),
                new AddMatchingPostRequest("웹툰 파트너를 구합니다", "writer", "hybrid", "무협 장르입니다. 웅장한 스토리 전개를 할 수 있는 분."),
                new AddMatchingPostRequest("웹툰 공동 제작할 작가님 찾습니다", "illustrator", "offline", "로맨스 판타지입니다. 몽환적인 분위기 연출 가능하신 분."),
                new AddMatchingPostRequest("웹툰 제작팀에서 함께할 분을 찾습니다", "writer", "online", "SF 장르 웹툰입니다. 미래적인 세계관 설정 가능하신 분."),
                new AddMatchingPostRequest("웹툰 팀에 합류하실 작가님을 구합니다", "illustrator", "hybrid", "판타지 모험물입니다. 배경과 캐릭터 연출이 조화로운 분."),
                new AddMatchingPostRequest("웹툰 시나리오 작가님 구합니다", "writer", "offline", "청춘 로맨스 장르 웹툰입니다. 감성적인 대사를 쓰는 분."),
                new AddMatchingPostRequest("웹툰 작화를 담당해 주실 분을 찾습니다", "illustrator", "online", "스릴러 장르입니다. 긴장감 있는 분위기를 연출할 수 있는 분."),
                new AddMatchingPostRequest("웹툰 제작 팀원 구합니다", "writer", "hybrid", "공포 장르입니다. 몰입감 있는 이야기 전개 가능하신 분."),
                new AddMatchingPostRequest("웹툰 작화가 모집합니다", "illustrator", "offline", "로맨틱 코미디 장르 웹툰입니다. 경쾌한 연출 가능하신 분."),
                new AddMatchingPostRequest("웹툰 작가 및 콘티 작가 구인", "writer", "online", "드라마 웹툰입니다. 현실적인 감정을 담을 수 있는 분."),
                new AddMatchingPostRequest("웹툰 일러스트레이터 구인", "illustrator", "hybrid", "판타지 액션 웹툰입니다. 캐릭터 디자인과 연출이 뛰어난 분."),
                new AddMatchingPostRequest("소설 작가 모집", "writer", "online", "로맨스 판타지 소설을 함께 집필할 작가님을 찾습니다."),
                new AddMatchingPostRequest("시나리오 작가 구합니다", "writer", "hybrid", "드라마틱한 전개를 잘 살리는 시나리오 작가님을 모집합니다."),
                new AddMatchingPostRequest("웹소설 작가님을 찾습니다", "writer", "offline", "스릴러 장르의 웹소설을 함께 만들어 갈 작가님을 찾습니다."),
                new AddMatchingPostRequest("웹소설 시나리오 작가 구인", "writer", "online", "현대 판타지 웹소설을 기획 중입니다. 경험 있는 분 환영!"),
                new AddMatchingPostRequest("장편 소설 작가님 모십니다", "writer", "hybrid", "로맨스 드라마 장편 소설을 함께 집필하실 작가님을 찾습니다."),
                new AddMatchingPostRequest("SF 소설 집필 작가 모집", "writer", "offline", "미래 배경의 SF 소설 프로젝트입니다. 세계관 구성에 자신 있는 분."),
                new AddMatchingPostRequest("무협 소설 작가 구합니다", "writer", "online", "전통적인 무협 스타일의 소설을 함께 써나갈 작가님을 모집합니다."),
                new AddMatchingPostRequest("판타지 소설 공동 집필", "writer", "hybrid", "서양 판타지 세계관 기반의 소설을 함께 창작할 분을 찾습니다."),
                new AddMatchingPostRequest("단편 소설 작가님을 구합니다", "writer", "offline", "여러 장르의 단편 소설을 함께 기획하실 분을 찾습니다."),
                new AddMatchingPostRequest("로맨스 소설 시나리오 작가 구인", "writer", "online", "감성적인 스토리를 잘 다룰 수 있는 작가님을 모집합니다."),
                new AddMatchingPostRequest("소설 공동 집필자 모집", "writer", "hybrid", "미스터리 장르 소설을 함께 써나갈 작가님을 구합니다."),
                new AddMatchingPostRequest("웹소설 집필 작가님을 찾습니다", "writer", "offline", "판타지와 현대가 결합된 색다른 설정의 웹소설을 함께 작업하실 분."),
                new AddMatchingPostRequest("드라마 시나리오 작가 모집", "writer", "online", "드라마틱한 전개를 선호하는 시나리오 작가님을 구합니다."),
                new AddMatchingPostRequest("스토리 작가님을 찾습니다", "writer", "hybrid", "모바일 웹소설 프로젝트에 참여하실 작가님을 모집합니다."),
                new AddMatchingPostRequest("웹소설 창작팀에서 작가 구인", "writer", "offline", "스릴러 & 범죄 소설을 함께 써 나가실 분을 찾습니다."),
                new AddMatchingPostRequest("청춘 소설 작가님 구합니다", "writer", "online", "젊은 감성을 담아낼 수 있는 청춘 로맨스 소설 작가님을 찾습니다."),
                new AddMatchingPostRequest("스릴러 소설 시나리오 작가 모집", "writer", "hybrid", "긴장감 넘치는 추리/스릴러 소설을 함께 집필할 작가님을 구합니다."),
                new AddMatchingPostRequest("소설 원고 작가님을 찾습니다", "writer", "offline", "출판 예정인 판타지 소설의 원고 작성을 맡아주실 작가님을 모집합니다."),
                new AddMatchingPostRequest("문학 소설 작가 모집", "writer", "online", "현대 문학 스타일의 깊이 있는 소설을 함께 집필할 작가님을 찾습니다."),
                new AddMatchingPostRequest("웹소설 플랫폼 연재 작가 구인", "writer", "hybrid", "웹소설 플랫폼에서 연재할 작품을 함께 만들 작가님을 모집합니다."),
                new AddMatchingPostRequest("로맨틱 판타지 소설 작가 구인", "writer", "offline", "달달한 로맨틱 판타지 장르에 관심 있는 작가님을 찾습니다."),
                new AddMatchingPostRequest("미스터리 스릴러 소설 작가님 모집", "writer", "online", "반전이 있는 미스터리 소설을 잘 쓰는 분을 찾습니다."),
                new AddMatchingPostRequest("무협 소설 집필 파트너 구인", "writer", "hybrid", "고전 무협 스타일을 함께 작업하실 작가님을 모집합니다."),
                new AddMatchingPostRequest("판타지 로맨스 소설 작가 모집", "writer", "offline", "로맨스와 판타지가 결합된 색다른 웹소설을 함께 써 나갈 분."),
                new AddMatchingPostRequest("소설 창작팀에서 작가 모집", "writer", "online", "다양한 장르의 소설을 실험적으로 써볼 작가님을 찾습니다."),
                new AddMatchingPostRequest("모바일 웹소설 작가 구합니다", "writer", "hybrid", "모바일 최적화된 웹소설 스토리를 쓸 작가님을 모집합니다."),
                new AddMatchingPostRequest("연재 소설 공동 집필자 모집", "writer", "offline", "장기 연재를 목표로 하는 스토리를 함께 기획할 작가님."),
                new AddMatchingPostRequest("역사 소설 작가님을 찾습니다", "writer", "online", "역사적 배경을 바탕으로 한 소설을 함께 집필할 분을 찾습니다."),
                new AddMatchingPostRequest("웹소설 집필을 함께할 작가 모집", "writer", "hybrid", "판타지, SF, 로맨스 등 다양한 장르의 작품을 함께 만들 작가님.")
        );

        Member member = memberRepository.save(createMember());
        for (AddMatchingPostRequest dummyRequest : dummyRequests) {

            matchingPostService.makeNewMatchingPost(member.getId(), dummyRequest);
        }
    }

//    @Test
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
