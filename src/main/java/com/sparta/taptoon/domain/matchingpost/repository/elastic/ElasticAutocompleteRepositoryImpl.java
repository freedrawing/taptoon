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
                                                .should(should -> should
                                                        // 정확한 prefix 매칭 (가장 높은 우선순위)
                                                        .prefix(prefix -> prefix
                                                                .field("word")
                                                                .value(searchText)
                                                                .boost(4.0f)
                                                        )
                                                )
                                                .should(should -> should
                                                        // NGram 기반 부분 매칭
                                                        .match(match -> match
                                                                .field("word.ngram")
                                                                .query(searchText)
                                                                .boost(3.0f)
                                                        )
                                                )
                                                .should(should -> should
                                                        // Nori 형태소 분석 기반 매칭
                                                        .match(match -> match
                                                                .field("word.nori")
                                                                .query(searchText)
                                                                .boost(2.0f)
                                                        )
                                                )
                                                .should(should -> should
                                                        // 오타 교정
                                                        .fuzzy(fuzzy -> fuzzy
                                                                .field("word")
                                                                .value(searchText)
                                                                .fuzziness("1")
                                                                .boost(1.0f)
                                                        )
                                                )
//                                .minimumShouldMatch(1)
                        )
        );
    }
}
