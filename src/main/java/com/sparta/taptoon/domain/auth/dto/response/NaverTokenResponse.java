package com.sparta.taptoon.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class NaverTokenResponse {
    private String accessToken;
    private String error;
    private String errorDescription;
}
