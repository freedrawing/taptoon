package com.sparta.taptoon.domain.auth.repository;

import com.sparta.taptoon.global.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "googleAuth", url = "https://oauth2.googleapis.com/token", configuration = FeignConfig.class)
public interface GoogleAuthClient extends OAuthClient{
}
