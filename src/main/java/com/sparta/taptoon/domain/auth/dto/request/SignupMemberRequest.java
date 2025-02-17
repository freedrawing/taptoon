package com.sparta.taptoon.domain.auth.dto.request;

import com.sparta.taptoon.domain.member.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Builder
public record SignupMemberRequest(
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotBlank(message = "이메일을 입력하세요.")
        String email,
        @NotBlank(message = "이름을 입력하세요.")
        String name,
        @NotBlank(message = "닉네임을 입력하세요.")
        String nickname,
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 영문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."
        )
        @NotBlank(message = "비밀번호를 입력하세요.")
        String password) {
    public Member toEntity(PasswordEncoder passwordEncoder) {
        return Member.builder()
                .email(email)
                .name(name)
                .nickname(nickname)
                .password(passwordEncoder.encode(password))
                .build();
    }
}
