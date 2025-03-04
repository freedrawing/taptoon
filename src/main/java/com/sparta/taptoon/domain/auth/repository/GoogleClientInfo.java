package com.sparta.taptoon.domain.auth.repository;

import com.sparta.taptoon.domain.auth.dto.response.OAuthApiResponse;
import com.sparta.taptoon.global.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "googleInfo", url = "https://www.googleapis.com/oauth2/v3", configuration = FeignConfig.class)
public interface GoogleClientInfo {
    @GetMapping("/userinfo")
    OAuthApiResponse.Response getUserInfo(@RequestHeader("Authorization") String bearerToken);
    default OAuthApiResponse getUserInfoWrapped(String bearerToken) {//구글은 최상위 객체에 데이터가 와서 래핑 필요
        OAuthApiResponse.Response response = getUserInfo(bearerToken);
        return new OAuthApiResponse(response);
    }
}
