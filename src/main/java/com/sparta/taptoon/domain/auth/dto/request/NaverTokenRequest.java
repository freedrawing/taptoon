package com.sparta.taptoon.domain.auth.dto.request;

import lombok.Builder;

@Builder
public record NaverTokenRequest(String grantType, String clientId, String clientSecret, String code, String state) {
}
