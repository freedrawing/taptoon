package com.sparta.taptoon.domain.matchingpost.repository.elastic;

import java.util.List;

public interface ElasticAutocompletedRepositoryCustom {

    List<String> searchAutocomplete(String keyword);
}
