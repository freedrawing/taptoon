package com.sparta.taptoon.domain.auth.controller;

import com.sparta.taptoon.domain.auth.dto.request.LoginMemberRequest;
import com.sparta.taptoon.domain.auth.dto.request.SignupMemberRequest;
import com.sparta.taptoon.domain.auth.dto.response.LoginMemberResponse;
import com.sparta.taptoon.domain.auth.dto.response.TokenInfo;
import com.sparta.taptoon.domain.auth.service.AuthService;
import com.sparta.taptoon.domain.member.dto.response.MemberResponse;
import com.sparta.taptoon.domain.member.entity.MemberDetail;
import com.sparta.taptoon.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<MemberResponse>> signUp(@RequestBody SignupMemberRequest request) {
        MemberResponse memberResponse = authService.signUp(request);
        return ApiResponse.created(memberResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginMemberResponse>> login(@RequestBody LoginMemberRequest request, HttpServletRequest httpServletRequest) {
        LoginMemberResponse loginResponse = authService.login(request, httpServletRequest);
        return ApiResponse.success(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenInfo>> refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        TokenInfo tokenInfo = authService.issueAccessTokenByRefreshToken(refreshToken);
        return ApiResponse.success(tokenInfo);
    }

    @PostMapping("/logout")
    public void logout(@AuthenticationPrincipal MemberDetail memberDetail, HttpServletRequest httpServletRequest) {
        authService.logout(memberDetail.getId(), httpServletRequest);
    }
}
