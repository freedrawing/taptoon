package com.sparta.taptoon.domain.auth.controller;

import com.sparta.taptoon.domain.auth.dto.request.LoginMemberRequest;
import com.sparta.taptoon.domain.auth.dto.request.SignupMemberRequest;
import com.sparta.taptoon.domain.auth.dto.response.LoginMemberResponse;
import com.sparta.taptoon.domain.auth.dto.response.TokenInfo;
import com.sparta.taptoon.domain.auth.service.AuthService;
import com.sparta.taptoon.domain.member.dto.response.MemberResponse;
import com.sparta.taptoon.domain.member.dto.MemberDetail;
import com.sparta.taptoon.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원 가입")
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<MemberResponse>> signUp(@Valid @RequestBody SignupMemberRequest request) {
        MemberResponse memberResponse = authService.signUp(request);
        return ApiResponse.created(memberResponse);
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginMemberResponse>> login(@Valid @RequestBody LoginMemberRequest request, HttpServletRequest httpServletRequest) {
        LoginMemberResponse loginResponse = authService.login(request, httpServletRequest);
        return ApiResponse.success(loginResponse);
    }

    @Operation(summary = "Access 토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenInfo>> refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        TokenInfo tokenInfo = authService.issueAccessTokenByRefreshToken(refreshToken);
        return ApiResponse.success(tokenInfo);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public void logout(@AuthenticationPrincipal MemberDetail memberDetail, HttpServletRequest httpServletRequest) {
        authService.logout(memberDetail.getId(), httpServletRequest);
    }


    @Operation(summary = "이메일 중복 체크 (true = 중복)")
    @PatchMapping("/check-email-duplicated")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailIsDuplicated(String email) {
        boolean emailAlreadyExist = authService.checkEmailAlreadyExist(email);
        return ApiResponse.success(emailAlreadyExist);
    }
}
