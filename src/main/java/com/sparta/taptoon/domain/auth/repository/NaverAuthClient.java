package com.sparta.taptoon.domain.auth.repository;

import com.sparta.taptoon.domain.auth.dto.request.NaverTokenRequest;
import com.sparta.taptoon.domain.auth.dto.response.NaverTokenResponse;
import com.sparta.taptoon.global.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "naverAuth", url = "https://nid.naver.com", configuration = FeignConfig.class)
public interface NaverAuthClient {
    @PostMapping("/oauth2.0/token")
    NaverTokenResponse getAccessToken(@RequestParam("grant_type") String grantType,
                                      @RequestParam("client_id") String clientId,
                                      @RequestParam("client_secret") String clientSecret,
                                      @RequestParam("code") String code,
                                      @RequestParam("state") String state);

    default NaverTokenResponse getAccessToken( NaverTokenRequest request) {
        return getAccessToken(request.grantType(),request.clientId(),request.clientSecret(),request.code(),request.state());
    };
}
