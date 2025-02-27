package com.sparta.taptoon.domain.matchingpost.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpdateMatchingPostRequest(
        @NotBlank(message = "제목은 필수 입력값입니다.") String title,
        @NotBlank(message = "세부사항은 필수 입력값입니다.") String description,
        @NotBlank(message = "작가타입은 필수 입력값입니다.") String artistType,
        @NotBlank(message = "업무형태는 필수 입력값입니다.") String workType,
        List<Long> matchingPostImageIds
) {

}
