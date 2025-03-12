package com.sparta.taptoon.domain.auth.repository;

import com.sparta.taptoon.domain.auth.dto.request.OAuthTokenRequest;
import com.sparta.taptoon.domain.auth.dto.response.OAuthTokenResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface OAuthClient {
    @PostMapping
    OAuthTokenResponse getAccessToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @RequestParam("redirect_uri") String redirectUri
    );

    default OAuthTokenResponse getAccessToken(OAuthTokenRequest request) {
        return getAccessToken(
                request.grantType(),
                request.clientId(),
                request.clientSecret(),
                request.code(),
                request.state(),
                request.redirectUrl()
        );
    }
}

