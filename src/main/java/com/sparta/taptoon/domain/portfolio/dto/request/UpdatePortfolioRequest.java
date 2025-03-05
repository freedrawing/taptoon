package com.sparta.taptoon.domain.portfolio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdatePortfolioRequest(
        @NotBlank(message = "제목을 입력해주세요.")
        String title,

        @NotBlank(message = "내용을 입력해주세요.")
        @Size(max = 3000, message = "최대 3,000자까지 입력할 수 있습니다.")
        String content,

        List<Long> validFileIds, // 유효한 Ids
        List<Long> deletedFileIds // 삭제 예정 Ids
) {
}
