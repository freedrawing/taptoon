package com.sparta.taptoon.domain.portfolio.dto.request;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;

@Builder
public record PortfolioRequest(
        @NotBlank(message = "제목을 입력해주세요.")
        String title,

        @NotBlank(message = "내용을 입력해주세요. 최대 3,000자")
        String content,

        // 파일 첨부는 첨부하지 않아도 허용
        String fileUrl,

        // s3로 부터 이미지 Url 리스트를 requestDto로 옮기기
        List<String> imageUrls
) {

    public Portfolio toEntity(Member member) {
        return Portfolio.builder()
                .member(member)
                .title(title)
                .content(content)
                .fileUrl(fileUrl)
                .build();
    }
}
