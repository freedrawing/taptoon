package com.sparta.taptoon.domain.auth.service;

import com.sparta.taptoon.domain.auth.dto.NaverMemberInfo;
import com.sparta.taptoon.domain.auth.dto.request.NaverTokenRequest;
import com.sparta.taptoon.domain.auth.dto.response.LoginMemberResponse;
import com.sparta.taptoon.domain.auth.dto.response.NaverApiResponse;
import com.sparta.taptoon.domain.auth.dto.response.NaverTokenResponse;
import com.sparta.taptoon.domain.auth.dto.response.TokenInfo;
import com.sparta.taptoon.domain.auth.entity.RefreshToken;
import com.sparta.taptoon.domain.auth.repository.NaverAuthClient;
import com.sparta.taptoon.domain.auth.repository.NaverClientInfo;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverAuthService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final NaverAuthClient naverAuthClient;
    private final NaverClientInfo naverClientInfo;

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
        NaverTokenRequest request = NaverTokenRequest.builder()
                .grantType("authorization_code")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .code(code)
                .state(state)
                .build();

        NaverTokenResponse response = naverAuthClient.getAccessToken(request);
        if (response.error() != null) {
            log.info("네이버 토큰 발급 실패 error: {}, discripsion:{}",response.error(), response.errorDescription());
            throw new RuntimeException("네이버에서 access token 획득 실패: " + response.errorDescription());
        }

        if (response.accessToken() == null) {
            throw new RuntimeException("Access token 을 찾을 수 없음!");
        }
        return response.accessToken();
    }

    private NaverMemberInfo getUserInfoFromNaver(String accessToken) {
        NaverApiResponse response = naverClientInfo.getUserInfo("Bearer " + accessToken);

        log.info("네이버 로그인 유저 id: {}, 유저 이름: {} ", response.response().id(), response.response().name());
        return NaverMemberInfo.builder()
                .id(response.response().id())
                .name(response.response().name())
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