package com.sparta.taptoon.domain.matchingpost.repository.elastic;

import com.sparta.taptoon.domain.matchingpost.entity.document.MatchingPostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticMatchingPostRepository
        extends ElasticsearchRepository<MatchingPostDocument, Long>, ElasticMatchingPostRepositoryCustom {

}
