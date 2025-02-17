package com.sparta.taptoon.domain.auth.controller;

import com.sparta.taptoon.domain.auth.dto.response.LoginMemberResponse;
import com.sparta.taptoon.domain.auth.service.NaverAuthService;
import com.sparta.taptoon.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "NaverAuth", description = "네이버 인증 API")
@RestController
@RequestMapping("/auth/naver")
@RequiredArgsConstructor
public class NaverAuthController {
    private final NaverAuthService naverAuthService;

    @Operation(summary = "네이버 로그인")
    @GetMapping("/login")
    public ResponseEntity<ApiResponse<Void>> naverLogin() {
        String naverAuthUrl = naverAuthService.getNaverAuthorizationUrl();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(naverAuthUrl))
                .build();
    }
    @Operation(summary = "네이버 로그인 콜백")
    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<LoginMemberResponse>> naverCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletRequest httpServletRequest
    ) {
        LoginMemberResponse loginResponse = naverAuthService.naverCallback(code, state, httpServletRequest);
        return ApiResponse.success(loginResponse);
    }
}
