package com.sparta.taptoon.domain.auth.repository;

import com.sparta.taptoon.global.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "naverAuth", url = "https://nid.naver.com/oauth2.0/token", configuration = FeignConfig.class)
public interface NaverAuthClient extends OAuthClient{
}
