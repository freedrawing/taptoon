package com.sparta.taptoon.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginMemberRequest(
        @NotBlank(message = "이메일을 입력하세요.")
        String email,
        @NotBlank(message = "비밀번호를 입력하세요.")
        String password) {
}
