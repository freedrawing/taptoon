package com.sparta.taptoon.domain.auth.controller;

import com.sparta.taptoon.domain.auth.dto.response.LoginMemberResponse;
import com.sparta.taptoon.domain.auth.service.NaverAuthService;
import com.sparta.taptoon.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag(name = "OAuth", description = "소셜 로그인 인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class OAuthController {
    private final NaverAuthService naverAuthService;
    private final String googleAuthUrl = "http://localhost:8080/oauth2/authorization/google";

    @Operation(summary = "네이버 로그인")
    @GetMapping("/naver/login")
    public ResponseEntity<ApiResponse<Void>> naverLogin() {
        String naverAuthUrl = naverAuthService.getNaverAuthorizationUrl();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(naverAuthUrl))
                .build();
    }

//    @Operation(summary = "네이버 로그인 콜백")
//    @GetMapping("/naver/callback")
//    public ResponseEntity<ApiResponse<LoginMemberResponse>> naverCallback(
//            @RequestParam String code,
//            @RequestParam String state,
//            HttpServletRequest httpServletRequest
//    ) {
//        LoginMemberResponse loginResponse = naverAuthService.naverCallback(code, state, httpServletRequest);
//        return ApiResponse.success(loginResponse);
//    }

    @Operation(summary = "네이버 로그인 콜백")
    @GetMapping("/naver/callback")
    public void naverCallback( // application.yaml에  redirect-uri은 왜 설정한 걸까?
            @RequestParam String code,
            @RequestParam String state,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) throws IOException {
        LoginMemberResponse loginResponse = naverAuthService.naverCallback(code, state, httpServletRequest);
        // 프론트엔드로 리디렉션 (토큰을 쿼리 파라미터로 전달)
        String redirectUrl = "http://localhost:3000/login?access_token=" + URLEncoder.encode(loginResponse.accessToken(), StandardCharsets.UTF_8)
                + "&refresh_token=" + URLEncoder.encode(loginResponse.refreshToken(), StandardCharsets.UTF_8) +
                "&expires_at=" + URLEncoder.encode(String.valueOf(loginResponse.tokenExpiresAt()), StandardCharsets.UTF_8);
        httpServletResponse.sendRedirect(redirectUrl);
    }

    @Operation(summary = "구글 로그인")
    @GetMapping("/google/login")
    public ResponseEntity<ApiResponse<Void>> googleLogin() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(googleAuthUrl))
                .build();
    }
}
