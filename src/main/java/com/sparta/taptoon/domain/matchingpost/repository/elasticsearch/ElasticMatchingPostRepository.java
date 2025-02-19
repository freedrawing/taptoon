package com.sparta.taptoon.domain.matchingpost.repository.elasticsearch;

import com.sparta.taptoon.domain.matchingpost.entity.document.MatchingPostDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ElasticMatchingPostRepository
        extends ElasticsearchRepository<MatchingPostDocument, Long>, ElasticMatchingPostRepositoryCustom {

    @Query("""
            {
              "bool": {
                "should": [
                  { "match": { "title": "?0" }},
                  { "match": { "description": "?0" }}
                ]
              }
            }
            """)
    List<Long> searchIdsByKeyword(String keyword);
}
