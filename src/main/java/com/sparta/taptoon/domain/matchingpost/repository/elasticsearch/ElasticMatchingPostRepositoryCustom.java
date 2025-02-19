package com.sparta.taptoon.domain.matchingpost.repository.elasticsearch;

import java.util.List;

public interface ElasticMatchingPostRepositoryCustom {

    List<Long> searchIdsByKeywordV2(String keyword);
}
