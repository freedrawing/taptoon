package com.sparta.taptoon.domain.auth.dto.request;

import com.sparta.taptoon.domain.member.entity.Member;
import lombok.Builder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Builder
public record SignupMemberRequest(String email, String name, String nickname, String password) {
    public Member toEntity(PasswordEncoder passwordEncoder) {
        return Member.builder()
                .email(email)
                .name(name)
                .nickname(nickname)
                .password(passwordEncoder.encode(password))
                .build();
    }
}
