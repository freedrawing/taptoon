package com.sparta.taptoon.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class NaverTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
    private String error;
    @JsonProperty("error_description")
    private String errorDescription;
}
