package com.sparta.taptoon.domain.member.service;

import com.sparta.taptoon.domain.auth.service.AuthService;
import com.sparta.taptoon.domain.member.dto.response.MemberResponse;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.enums.MemberGrade;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public void setMemberEmail(Member member, String email) {
        if(member.getEmail()!= null) {
            throw new InvalidRequestException(ErrorCode.ACCESS_DENIED);
        }
        member.setFirstEmail(email);
        memberRepository.save(member);
    }

    public void changeUserPassword(Member member, String newPassword) {
        if(member.getPassword() != null && member.getPassword().equals(newPassword)) {
            throw new InvalidRequestException(ErrorCode.SAME_VALUE_REQUEST);
        }
        String encodedPassword = passwordEncoder.encode(newPassword);
        member.changePassword(encodedPassword);
        memberRepository.save(member);
        authService.logoutAllDevice(member.getId());
    }

    public void changeUserNickname(Member member, String newNickname) {
        if(member.getNickname() != null && member.getNickname().equals(newNickname)) {
            throw new InvalidRequestException(ErrorCode.SAME_VALUE_REQUEST);
        }
        member.changeNickname(newNickname);
        memberRepository.save(member);
    }

    public void changeUserGrade(Member member, MemberGrade newGrade) {
        if(member.getGrade().name().equals(newGrade.name())) {
            throw new InvalidRequestException();
        }
        member.changeGrade(newGrade);
        memberRepository.save(member);
    }

    public MemberResponse findMember(Member member) {
        return MemberResponse.from(member);
    }

    public Page<MemberResponse> findMemberByNameOrNickname(String name, String nickname, Pageable pageable) {
        Page<Member> members = memberRepository.findByNameOrNickname(name, nickname, pageable);
        return members.map(MemberResponse::from);
    }

    public void removeMember(Member member) {
        member.withdrawMember();
        memberRepository.save(member);
    }
}
