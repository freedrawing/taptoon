package com.sparta.taptoon.domain.matchingpost.repository.elastic;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.sparta.taptoon.domain.matchingpost.entity.document.AutocompleteDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ElasticAutocompleteRepositoryImpl implements ElasticAutocompletedRepositoryCustom {

    private final ElasticsearchOperations elasticsearchOperations;

    // Autocomplete
    @Override
    public List<String> searchAutocomplete(String keyword) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(createAutocompleteQuery(keyword))
                .withSort(Sort.by(
                                Sort.Order.desc("_score"),
                                Sort.Order.desc("searchCount")
                        )
                )
                .withPageable(PageRequest.of(0, 10))
                .build();

        SearchHits<AutocompleteDocument> searchHits =
                elasticsearchOperations.search(query, AutocompleteDocument.class);

        return searchHits.stream()
                .map(SearchHit::getContent)
                .map(AutocompleteDocument::getWord)  // AutocompleteDocument에서 word 필드만 추출
                .collect(Collectors.toList());
    }

    private Query createAutocompleteQuery(String searchText) {
        return Query.of(query ->
                        query.bool(bool ->
                                        bool
                                                // 정확한 구문 매칭 (가장 높은 우선순위)
                                                .should(should ->
                                                        should.matchPhrase(mp -> mp
                                                                .field("word.keyword")
                                                                .query(searchText)
                                                                .boost(3.0f)
                                                        )
                                                )
                                                // 시작 부분 매칭
                                                .should(should ->
                                                        should.matchPhrasePrefix(mpp -> mpp
                                                                .field("word")
                                                                .query(searchText)
                                                                .boost(2.0f)
                                                        )
                                                )
                                                // 오타나 유사어 검색: keyword 필드에서 fuzzy 검색 수행
                                                .should(should ->
                                                        should.fuzzy(fuzzy -> fuzzy
                                                                .field("word.keyword")
                                                                .value(searchText)
                                                                .fuzziness("1")
                                                        )
                                                )
                                                // 부분 매칭 (가장 낮은 우선순위)
                                                .should(should ->
                                                        should.match(m -> m
                                                                .field("word")
                                                                .query(searchText)
                                                                .boost(1.0f)
                                                        )
                                                )
                        )
        );
    }
}
