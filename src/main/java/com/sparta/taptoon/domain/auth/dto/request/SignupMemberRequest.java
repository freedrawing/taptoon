package com.sparta.taptoon.domain.auth.dto.request;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.enums.MemberGrade;
import lombok.Builder;

@Builder
public record SignupMemberRequest(String email, String name, String nickname, String password, MemberGrade grade) {
    public Member toEntity() {
        return Member.builder()
                .email(email)
                .name(name)
                .nickname(nickname)
                .password(password)
                .grade(grade)
                .build();
    }
}
