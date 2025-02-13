package com.sparta.taptoon.domain.auth.service;

import com.sparta.taptoon.domain.auth.dto.request.LoginMemberRequest;
import com.sparta.taptoon.domain.auth.dto.request.SignupMemberRequest;
import com.sparta.taptoon.domain.member.dto.response.MemberResponse;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.error.exception.InvalidRequestException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;

    public MemberResponse signUp(SignupMemberRequest request) {
        Member member = request.toEntity();
        Member savedMember = memberRepository.save(member);
        return MemberResponse.from(savedMember);
    }

    public String login(LoginMemberRequest request) {
        Member member = memberRepository.findByEmail(request.email()).orElseThrow(NotFoundException::new);
        /**
         * 비밀번호 검증 로직
         */
        if (!member.getPassword().equals(request.password())) {
            throw new InvalidRequestException();
        }
        /**
         * 토큰 발급 로직
         */
        String accessToken = "accessToken";
        return accessToken;
    }
}
