package com.sparta.taptoon.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuthTokenResponse(@JsonProperty("access_token") String accessToken,
                                 @JsonProperty("refresh_token") String refreshToken,
                                 @JsonProperty("token_type") String tokenType,
                                 @JsonProperty("expires_in") Integer expiresIn,
                                 @JsonProperty("error") String error,
                                 @JsonProperty("error_description") String errorDescription) {
}
