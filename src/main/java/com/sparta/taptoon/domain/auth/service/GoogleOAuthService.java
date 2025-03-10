package com.sparta.taptoon.domain.auth.service;

import com.sparta.taptoon.domain.auth.dto.OAuthMemberInfo;
import com.sparta.taptoon.domain.auth.dto.request.OAuthTokenRequest;
import com.sparta.taptoon.domain.auth.dto.response.LoginMemberResponse;
import com.sparta.taptoon.domain.auth.dto.response.OAuthApiResponse;
import com.sparta.taptoon.domain.auth.dto.response.OAuthTokenResponse;
import com.sparta.taptoon.domain.auth.repository.GoogleAuthClient;
import com.sparta.taptoon.domain.auth.repository.GoogleClientInfo;
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
public class GoogleOAuthService implements OAuthProviderService{
    private final OAuthService oAuthService;
    private final GoogleAuthClient googleAuthClient;
    private final GoogleClientInfo googleClientInfo;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Override
    public String getAuthorizationUrl() {
        String state = UUID.randomUUID().toString();
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&scope=openid%20email%20profile"
                + "&state=" + state;
    }

    @Transactional
    public LoginMemberResponse googleCallback(String code, String state, HttpServletRequest request) {
        String accessToken = getAccessToken(code, state);
        OAuthMemberInfo googleMemberInfo = getMemberInfo(accessToken);
        return oAuthService.processLogin(OAuthProvider.GOOGLE, googleMemberInfo, request);
    }

    @Override
    public String getAccessToken(String code, String state) {
        OAuthTokenRequest request = OAuthTokenRequest.builder()
                .grantType("authorization_code")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .code(code)
                .state(state)
                .redirectUrl(redirectUri)
                .build();
        log.info("구글 토큰 요청: {}", request);
        OAuthTokenResponse response = googleAuthClient.getAccessToken(request);
        log.info("구글 토큰 응답: {}", response);
        if (response.error() != null) {
            log.info("구글 토큰 발급 실패 error: {}, description: {}", response.error(), response.errorDescription());
            throw new RuntimeException("구글에서 access token 획득 실패: " + response.errorDescription());
        }
        if (response.accessToken() == null) {
            throw new RuntimeException("Access token을 찾을 수 없음!");
        }
        return response.accessToken();
    }

    @Override
    public OAuthMemberInfo getMemberInfo(String accessToken) {
        OAuthApiResponse response = googleClientInfo.getUserInfoWrapped("Bearer " + accessToken);
        log.info("구글 로그인 유저 id: {}, 유저 이름: {}", response.response().getProviderId(), response.response().name());
        return OAuthMemberInfo.from(response.response());
    }

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.GOOGLE;
    }
}
