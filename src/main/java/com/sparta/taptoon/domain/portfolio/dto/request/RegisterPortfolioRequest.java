package com.sparta.taptoon.domain.portfolio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record RegisterPortfolioRequest(
        @NotBlank(message = "제목을 입력해주세요.")
        String title,

        @NotBlank(message = "내용을 입력해주세요.")
        @Size(max = 3000, message = "최대 3,000자까지 입력할 수 있습니다.")
        String content,

        // 이미지 첨부하기
        List<Long> portfolioFileIds
) {
}
