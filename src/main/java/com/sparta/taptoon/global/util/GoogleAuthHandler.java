package com.sparta.taptoon.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.taptoon.domain.auth.dto.response.LoginMemberResponse;
import com.sparta.taptoon.domain.auth.service.GoogleAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class GoogleAuthHandler implements AuthenticationSuccessHandler {
    private final GoogleAuthService googleAuthService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        LoginMemberResponse loginResponse = googleAuthService.googleLogin(oauth2User);

        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(loginResponse));
    }
}
