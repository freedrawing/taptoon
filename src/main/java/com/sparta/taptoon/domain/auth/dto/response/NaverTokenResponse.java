package com.sparta.taptoon.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public record NaverTokenResponse(@JsonProperty("access_token") String accessToken,
                                 @JsonProperty("refresh_token") String refreshToken,
                                 @JsonProperty("token_type") String tokenType,
                                 @JsonProperty("expires_in") Integer expiresIn,
                                 @JsonProperty("error") String error,
                                 @JsonProperty("error_description") String errorDescription) {
}
