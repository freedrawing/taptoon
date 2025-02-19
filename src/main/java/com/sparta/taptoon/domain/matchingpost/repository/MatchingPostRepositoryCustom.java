package com.sparta.taptoon.domain.matchingpost.repository;

import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostResponse;
import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MatchingPostRepositoryCustom {

    Page<MatchingPostResponse> searchMatchingPostsFromCondition(ArtistType artistType, WorkType workType, String keyword, Pageable pageable);
    Page<MatchingPostResponse> searchMatchingPostsFromConditionV2(ArtistType artistType, WorkType workType, List<Long> ids, Pageable pageable);
}
