package com.sparta.taptoon.domain.member.dto.response;

import com.sparta.taptoon.domain.member.entity.Member;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MemberResponse(Long id, String email, String name, String nickname, String grade, LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .grade(member.getGrade().toString())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
