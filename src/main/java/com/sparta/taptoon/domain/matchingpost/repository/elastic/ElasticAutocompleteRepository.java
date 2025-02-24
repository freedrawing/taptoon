package com.sparta.taptoon.domain.matchingpost.repository.elastic;

import com.sparta.taptoon.domain.matchingpost.entity.document.AutocompleteDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticAutocompleteRepository extends ElasticsearchRepository<AutocompleteDocument, String>, ElasticAutocompletedRepositoryCustom {
}
