package com.sparta.taptoon.domain.matchingpost.repository.elasticsearch;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.json.JsonData;
import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostCursorResponse;
import com.sparta.taptoon.domain.matchingpost.entity.document.MatchingPostDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "Elasticsearch:MatchingPost")
@RequiredArgsConstructor
public class ElasticMatchingPostRepositoryImpl implements ElasticMatchingPostRepositoryCustom {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public MatchingPostCursorResponse searchFrom(
            String artistType,
            String workType,
            String keyword,
            Long lastViewCount,
            Long lastId,
            Integer pageSize
    ) {
        // Bool Query 구성
        Query boolQuery = Query.of(query -> query
                .bool(builder -> {

                    // keyword 검색 조건 추가
                    if (StringUtils.hasText(keyword)) {
                        builder.should(createKeywordQuery(keyword));
                    }

                    // artistType 조건 추가
                    if (StringUtils.hasText(artistType)) {
                        builder.must(createTypeTermQuery("artistType", artistType));
                    }

                    // workType 조건 추가
                    if (StringUtils.hasText(workType)) {
                        builder.must(createTypeTermQuery("workType", workType));
                    }

                    // 커서 기반 페이징 필터
                    if (lastViewCount != null && lastId != null) {
                        builder.filter(createCursorFilter(lastViewCount, lastId));
                    }

                    return builder;
                })
        );

        // NativeQuery 생성
        NativeQuery query = NativeQuery.builder()
                .withQuery(boolQuery)
                .withSort(createSortBuilders())  // 정렬 기준 추가
                .withMaxResults(pageSize)        // 페이지 크기 지정
                .withTrackScores(true)  // 점수 추적 활성화
                .build();

        // Elasticsearch 검색 실행
        SearchHits<MatchingPostDocument> searchHits = elasticsearchOperations.search(
                query,
                MatchingPostDocument.class
        );

        // 검색 결과 변환
        List<MatchingPostDocument> results = searchHits.stream()
//                .map(SearchHit::getContent)
                .map(searchHit -> {
                    MatchingPostDocument document = searchHit.getContent();

                    float score = searchHit.getScore();
                    log.info("{} score: {}", document, score);

                    return document;
                })
                .toList();

        Long nextViewCount = null;
        Long nextId = null;
        if (!results.isEmpty()) {
            MatchingPostDocument lastDoc = results.get(results.size() - 1);
            nextViewCount = lastDoc.getViewCount();
            nextId = lastDoc.getId();
        }

        // 클라이언트에게 현재 데이터 + 다음 페이지 요청을 위한 커서 반환
        return new MatchingPostCursorResponse(results, nextId, nextViewCount);
    }

    /**
     * 커서 기반 페이징을 위한 필터 생성
     * viewCount가 lastViewCount보다 작은 문서들을 가져오거나
     * viewCount가 lastViewCount와 같고 id가 lastId보다 작은 문서들을 가져옴
     * must(): AND 조건 (모든 조건 만족해야 함)
     * should(): OR 조건 (두 가지 경우 중 하나를 만족)
     *
     */
    private Query createCursorFilter(Long lastViewCount, Long lastId) {
        return Query.of(filter -> filter.bool(boolFilter -> boolFilter
                .should(shouldQuery -> shouldQuery
                        .bool(boolQuery -> boolQuery
                                .must(mustQuery -> mustQuery.range(rangeQuery -> rangeQuery
                                        .field("viewCount")
                                        .lt(JsonData.of(lastViewCount)))) // 1️⃣ 조회수가 lastViewCount보다 작은 데이터
                                .must(mustQuery -> mustQuery.range(rangeQuery -> rangeQuery
                                        .field("id")
                                        .lt(JsonData.of(lastId)))) // 2️⃣ id가 lastId보다 작은 데이터
                        ))
                .should(shouldQuery -> shouldQuery
                        .bool(boolQuery -> boolQuery
                                .must(mustQuery -> mustQuery.term(termQuery -> termQuery
                                        .field("viewCount")
                                        .value(lastViewCount))) // 3️⃣ 조회수가 lastViewCount와 동일한 데이터
                                .must(mustQuery -> mustQuery.range(rangeQuery -> rangeQuery
                                        .field("id")
                                        .lt(JsonData.of(lastId)))) // 4️⃣ id가 lastId보다 작은 데이터
                        ))
        ));
    }

    /**
     * 키워드 검색을 위한 Query 생성
     * Query.of(): Elasticsearch 쿼리 생성
     * multiMatch(): 여러 필드에 걸쳐 검색하는 쿼리 타입
     */
    private Query createKeywordQuery(String keyword) {
        return Query.of(search -> search.multiMatch(multiMatch -> multiMatch
                .query(keyword)
                .fields("title", "title.english", "description", "description.english")
                .type(TextQueryType.BestFields) // 가장 잘 매칭되는 필드를 우선적으로 채택. 여러 필드 중 가낭 높은 점수 매칭 결과 반환
        ));
    }

    /**
     * Term Query 생성 (artistType, workType 검색용)
     * `Term Query`는 전체 텍스트 분석 과정 없이 정확히 일치하는 값 찾음. 대소문자 구분함. 그래서 normalizer = "lowercase" <- 설정해줘야 함.
     */
    private Query createTypeTermQuery(String field, String value) {
        return Query.of(matchQuery -> matchQuery.term(termQuery -> termQuery
                .field(field) // 검색할 필드 이름
                .value(value) // 해당 필드에서 정확히 일치해야 함
        ));
    }

    /**
     * 정렬 조건 생성 (viewCount DESC, id DESC)
     * viewCount 내림차순 정렬 (높은 것부터)
     * 같은 viewCount 내에서는 id 내림차순 정렬
     * ✅viewCount로만 하면 조회가 빈번하게 할 때는 문제가 발생한다. 우선은 viewCount로만 로직을 구성했으나, 향후 빈번한 조회를 생각해
     * 1. createdAt 같은 컬럼을 추가로 사용하거나,
     * 2. 조회가 발생할 때 그때마다 업데이트하는 대신, 주기적으로 bulk 업데이트를 고려해봐야할 듯하다.
     */
    private List<SortOptions> createSortBuilders() {
        List<SortOptions> sortOptions = new ArrayList<>();

        // 1️⃣ Elasticsearch 기본 score(검색 점수) 기반 정렬 (높은 점수 우선)
        sortOptions.add(SortOptions.of(sort -> sort
                .score(scoreSort -> scoreSort.order(SortOrder.Desc))));

        // 2️⃣ 조회수 내림차순 정렬
        sortOptions.add(SortOptions.of(sort -> sort
                .field(fieldSort -> fieldSort
                        .field("viewCount")
                        .order(SortOrder.Desc))));

        // 3️⃣ 같은 조회수일 경우 id 내림차순 정렬
        sortOptions.add(SortOptions.of(sort -> sort
                .field(fieldSort -> fieldSort
                        .field("id")
                        .order(SortOrder.Desc))));

        return sortOptions;
    }
}