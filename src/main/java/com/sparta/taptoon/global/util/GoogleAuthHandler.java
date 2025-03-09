package com.sparta.taptoon.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.taptoon.domain.auth.dto.response.LoginMemberResponse;
import com.sparta.taptoon.domain.auth.service.GoogleAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthHandler implements AuthenticationSuccessHandler {
    private final GoogleAuthService googleAuthService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        LoginMemberResponse loginResponse = googleAuthService.googleLogin(oauth2User);
        log.info("사용자 구글 로그인 시도 이름: {}, access_token : {} ",oauth2User.getName(), loginResponse.accessToken());

        response.setContentType("application/json");
//        response.getWriter().write(objectMapper.writeValueAsString(loginResponse));
        // 프론트엔드로 리디렉션 (토큰을 쿼리 파라미터로 전달)
        String redirectUrl = "https://taptoon.site/login?access_token=" + URLEncoder.encode(loginResponse.accessToken(), StandardCharsets.UTF_8)
                + "&refresh_token=" + URLEncoder.encode(loginResponse.refreshToken(), StandardCharsets.UTF_8) +
                "&expires_at=" + URLEncoder.encode(String.valueOf(loginResponse.tokenExpiresAt()), StandardCharsets.UTF_8);
        response.sendRedirect(redirectUrl);
    }
}
