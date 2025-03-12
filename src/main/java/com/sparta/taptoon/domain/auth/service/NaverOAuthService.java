package com.sparta.taptoon.domain.auth.service;

import com.sparta.taptoon.domain.auth.dto.OAuthMemberInfo;
import com.sparta.taptoon.domain.auth.dto.request.OAuthTokenRequest;
import com.sparta.taptoon.domain.auth.dto.response.LoginMemberResponse;
import com.sparta.taptoon.domain.auth.dto.response.OAuthApiResponse;
import com.sparta.taptoon.domain.auth.dto.response.OAuthTokenResponse;
import com.sparta.taptoon.domain.auth.repository.NaverAuthClient;
import com.sparta.taptoon.domain.auth.repository.NaverClientInfo;
import com.sparta.taptoon.domain.member.enums.OAuthProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverOAuthService implements OAuthProviderService{
    private final OAuthService oAuthService;
    private final NaverAuthClient naverAuthClient;
    private final NaverClientInfo naverClientInfo;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String redirectUri;
    @Override
    public String getAuthorizationUrl() {
        String state = UUID.randomUUID().toString();

        return "https://nid.naver.com/oauth2.0/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&state=" + state;
    }

    @Transactional
    public LoginMemberResponse naverCallback(String code, String state, HttpServletRequest request) {
        String accessToken = getAccessToken(code, state);
        OAuthMemberInfo OAuthMemberInfo = getMemberInfo(accessToken);
        return oAuthService.processLogin(OAuthProvider.NAVER, OAuthMemberInfo, request);
    }

    @Override
    public String getAccessToken(String code, String state) {
        OAuthTokenRequest request = OAuthTokenRequest.builder()
                .grantType("authorization_code")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .code(code)
                .state(state)
                .build();

        OAuthTokenResponse response = naverAuthClient.getAccessToken(request);
        if (response.error() != null) {
            log.info("네이버 토큰 발급 실패 error: {}, discripsion:{}",response.error(), response.errorDescription());
            throw new RuntimeException("네이버에서 access token 획득 실패: " + response.errorDescription());
        }

        if (response.accessToken() == null) {
            throw new RuntimeException("Access token 을 찾을 수 없음!");
        }
        return response.accessToken();
    }

    @Override
    public OAuthMemberInfo getMemberInfo(String accessToken) {
        OAuthApiResponse response = naverClientInfo.getUserInfo("Bearer " + accessToken);

        log.info("네이버 로그인 유저 id: {}, 유저 이름: {} ", response.response().id(), response.response().name());
        return OAuthMemberInfo.from(response.response());
    }

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.NAVER;
    }
}