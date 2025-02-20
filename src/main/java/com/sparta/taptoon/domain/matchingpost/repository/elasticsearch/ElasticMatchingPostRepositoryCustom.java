package com.sparta.taptoon.domain.matchingpost.repository.elasticsearch;

import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostCursorResponse;

public interface ElasticMatchingPostRepositoryCustom {

    MatchingPostCursorResponse searchFrom(
            String artistType,
            String workType,
            String keyword,
            Long lastViewCount,
            Long lastId,
            Integer pageSize
    );
}
