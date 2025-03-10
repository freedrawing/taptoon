package com.sparta.taptoon.domain.auth.dto.response;

import lombok.Builder;

public record OAuthApiResponse(Response response) {
    @Builder
    public record Response(
            String id,// 네이버의 id
            String sub,// 구글의 sub
            String name
    ) {
        // 제공자별 ID를 통합적으로 가져오는 메서드 추가
        public String getProviderId() {
            return id != null ? id : sub;
        }
    }
}
