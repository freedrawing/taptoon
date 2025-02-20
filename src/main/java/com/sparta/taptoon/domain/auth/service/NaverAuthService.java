package com.sparta.taptoon.domain.auth.service;

import com.sparta.taptoon.domain.auth.dto.NaverMemberInfo;
import com.sparta.taptoon.domain.auth.dto.response.LoginMemberResponse;
import com.sparta.taptoon.domain.auth.dto.response.NaverApiResponse;
import com.sparta.taptoon.domain.auth.dto.response.NaverTokenResponse;
import com.sparta.taptoon.domain.auth.dto.response.TokenInfo;
import com.sparta.taptoon.domain.auth.entity.RefreshToken;
import com.sparta.taptoon.domain.auth.repository.RefreshTokenRepository;
import com.sparta.taptoon.domain.member.dto.MemberDetail;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.enums.OAuthProvider;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverAuthService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String redirectUri;

    public String getNaverAuthorizationUrl() {
        String state = UUID.randomUUID().toString();

        return "https://nid.naver.com/oauth2.0/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&state=" + state;
    }

    @Transactional
    public LoginMemberResponse naverCallback(String code, String state, HttpServletRequest request) {
        String accessToken = getAccessTokenFromNaver(code, state);
        NaverMemberInfo naverMemberInfo = getUserInfoFromNaver(accessToken);
        return naverLogin(naverMemberInfo, request);
    }

    private String getAccessTokenFromNaver(String code, String state) {
        String tokenRequestUrl = "https://nid.naver.com/oauth2.0/token";
        log.info("code 값: {} state 값: {}",code,state);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("state", state);

        WebClient webClient = WebClient.create();
        NaverTokenResponse response = webClient.post()
                .uri(tokenRequestUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(params)
                .retrieve()
                .bodyToMono(NaverTokenResponse.class)
                .block();

        if (response.getError() != null) {
            log.info("에러메세지: {}",response.getError());
            throw new RuntimeException("네이버에서 access token 획득 실패: " + response.getErrorDescription());
        }

        if (response.getAccessToken() == null) {
            throw new RuntimeException("Access token 을 찾을 수 없음!");
        }
        return response.getAccessToken();
    }

    private NaverMemberInfo getUserInfoFromNaver(String accessToken) {
        String apiUrl = "https://openapi.naver.com/v1/nid/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        WebClient webClient = WebClient.create();
        NaverApiResponse response = webClient.get()
                .uri(apiUrl)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .bodyToMono(NaverApiResponse.class)
                .block();
        log.info("네이버 로그인 유저 id: {}, 유저 이름: {} ", response.getResponse().getId(), response.getResponse().getName());
        return NaverMemberInfo.builder()
                .id(response.getResponse().getId())
                .name(response.getResponse().getName())
                .build();
    }

    @Transactional
    public LoginMemberResponse naverLogin(NaverMemberInfo naverMemberInfo, HttpServletRequest httpServletRequest) {
        Member member = memberRepository.findByProviderAndProviderId(OAuthProvider.NAVER, naverMemberInfo.getId())
                .orElseGet(() -> registerNaverMember(naverMemberInfo));
        // 토큰 발급 및 저장
        Authentication authentication = createAuthentication(member);
        TokenInfo accessToken = jwtUtil.generateAccessToken(authentication);
        TokenInfo refreshTokenInfo = jwtUtil.generateRefreshToken(authentication);

        saveRefreshToken(member, refreshTokenInfo, httpServletRequest);

        return new LoginMemberResponse(
                accessToken.token(),
                refreshTokenInfo.token(),
                accessToken.expiresAt()
        );
    }

    private Member registerNaverMember(NaverMemberInfo naverMemberInfo) {
        Member member = Member.builder()
                .provider(OAuthProvider.NAVER)
                .providerId(naverMemberInfo.getId())
                .email(naverMemberInfo.getEmail())
                .password("")
                .name(naverMemberInfo.getName())
                .build();

        return memberRepository.save(member);
    }

    private Authentication createAuthentication(Member member) {
        MemberDetail memberDetail = new MemberDetail(member);
        return new UsernamePasswordAuthenticationToken(
                memberDetail,
                null,
                memberDetail.getAuthorities()
        );
    }

    private void saveRefreshToken(Member member, TokenInfo refreshTokenInfo, HttpServletRequest httpServletRequest) {
        String deviceInfo = getMemberDeviceInfo(httpServletRequest);
        RefreshToken refreshToken = RefreshToken.builder()
                .member(member)
                .token(refreshTokenInfo.token())
                .deviceInfo(deviceInfo)
                .expiresAt(refreshTokenInfo.expiresAt())
                .build();

        refreshTokenRepository.save(refreshToken);
    }
    private String getMemberDeviceInfo(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getHeader("User-Agent");
    }
}