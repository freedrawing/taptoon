package com.sparta.taptoon.domain.matchingpost.repository.elasticsearch;

import com.sparta.taptoon.domain.matchingpost.entity.document.MatchingPostDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class ElasticMatchingPostRepositoryImpl implements ElasticMatchingPostRepositoryCustom {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<Long> searchIdsByKeyword(String keyword) {

        if (StringUtils.hasText(keyword) == false) {
            return Collections.emptyList();
        }

        Query query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .should(s -> s
                                        .match(m -> m
                                                .field("title")
                                                .query(keyword)
//                                                .boost(2.0f)
                                        ))
                                .should(s -> s
                                        .match(m -> m
                                                .field("description")
                                                .query(keyword)
                                        ))
                        ))
                .withFields("id")
                .build();

        SearchHits<MatchingPostDocument> searchHits = elasticsearchOperations.search(
                query,
                MatchingPostDocument.class
        );

        return searchHits.stream()
                .map(hit -> hit.getContent().getId())
                .toList();
    }
}
