package com.sparta.taptoon.domain.portfolio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record UpdatePortfolioRequest(
        @NotBlank(message = "제목을 입력해주세요.")
        String title,

        @NotBlank(message = "내용을 입력해주세요.")
        @Size(max = 3000, message = "최대 3,000자까지 입력할 수 있습니다.")
        String content,

        // 파일 첨부는 첨부하지 않아도 허용
        String fileUrl,

        // 삭제할 포트폴리오 이미지 아이디
        List<Long> portfolioImageIdsToDel
) {
}
