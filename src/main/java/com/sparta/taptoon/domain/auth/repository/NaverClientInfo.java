package com.sparta.taptoon.domain.auth.repository;

import com.sparta.taptoon.domain.auth.dto.response.NaverApiResponse;
import com.sparta.taptoon.global.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "naverInfo", url = "https://openapi.naver.com", configuration = FeignConfig.class)
public interface NaverClientInfo {
    @GetMapping("/v1/nid/me")
    NaverApiResponse getUserInfo(@RequestHeader("Authorization") String bearerToken);
}
