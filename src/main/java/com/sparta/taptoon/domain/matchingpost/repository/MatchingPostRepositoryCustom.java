package com.sparta.taptoon.domain.matchingpost.repository;

import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostResponse;
import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MatchingPostRepositoryCustom {

    Page<MatchingPostResponse> searchMatchingPostsFromCondition(ArtistType artistType, WorkType workType, String keyword, Pageable pageable);
}
