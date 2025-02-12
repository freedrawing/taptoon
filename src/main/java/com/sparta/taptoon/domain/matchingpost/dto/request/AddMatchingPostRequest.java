package com.sparta.taptoon.domain.matchingpost.dto.request;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;
import com.sparta.taptoon.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;

public record AddMatchingPostRequest(
        @NotBlank(message = "제목은 필수값입니다.")
        String title,

        @NotBlank(message = "artistType은 필수값입니다.")
        String artistType,

        @NotBlank(message = "workType은 필수값입니다.")
        String workType,

        @NotBlank(message = "프로젝트 소개는 필수값입니다.")
        String description
) {

    public MatchingPost toEntity(User user) {
        return MatchingPost.builder()
                .writer(user)
                .artistType(ArtistType.of(artistType))
                .title(title)
                .workType(WorkType.of(workType))
                .description(description)
                .build();
    }
}
