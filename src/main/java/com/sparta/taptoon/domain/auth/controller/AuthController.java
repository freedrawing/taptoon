package com.sparta.taptoon.domain.auth.controller;

import com.sparta.taptoon.domain.auth.dto.request.LoginMemberRequest;
import com.sparta.taptoon.domain.auth.dto.request.SignupMemberRequest;
import com.sparta.taptoon.domain.auth.dto.response.LoginMemberResponse;
import com.sparta.taptoon.domain.auth.service.AuthService;
import com.sparta.taptoon.domain.member.dto.response.MemberResponse;
import com.sparta.taptoon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ApiResponse<LoginMemberResponse>> login(@RequestBody LoginMemberRequest request) {
        LoginMemberResponse loginResponse = authService.login(request);
        return ApiResponse.success(loginResponse);
    }
}
