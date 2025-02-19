package com.sparta.taptoon.domain.matchingpost.repository.elasticsearch;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ElasticMatchingPostRepositoryCustom {

    List<Long> searchIdsByKeyword(String keyword);
}
