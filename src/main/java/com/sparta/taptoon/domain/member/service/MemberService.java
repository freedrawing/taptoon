package com.sparta.taptoon.domain.member.service;

import com.sparta.taptoon.domain.auth.service.AuthService;
import com.sparta.taptoon.domain.member.dto.response.MemberResponse;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.enums.MemberGrade;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.error.exception.InvalidRequestException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void setMemberEmail(Long memberId, String email) {
        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundException::new);
        if(member.getEmail()!= null) {
            throw new InvalidRequestException();//exception 처리 필요
        }
        member.setFirstEmail(email);
    }

    @Transactional
    public void changeUserPassword(Long memberId, String newPassword) {
        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundException::new);
        if(member.getPassword() != null && member.getPassword().equals(newPassword)) {
            throw new InvalidRequestException();//exception 처리 필요
        }
        String encodedPassword = passwordEncoder.encode(newPassword);
        member.changePassword(encodedPassword);
        authService.logoutAllDevice(member.getId());
    }

    @Transactional
    public void changeUserNickname(Long memberId, String newNickname) {
        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundException::new);
        if(member.getNickname() != null && member.getNickname().equals(newNickname)) {
            throw new InvalidRequestException();//exception 처리 필요
        }
        member.changeNickname(newNickname);
    }

    @Transactional
    public void changeUserGrade(Long memberId, MemberGrade newGrade) {
        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundException::new);
        if(member.getGrade().name().equals(newGrade.name())) {
            throw new InvalidRequestException();
        }
        member.changeGrade(newGrade);
    }

    public MemberResponse findMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundException::new);
        return MemberResponse.from(member);
    }

    @Transactional
    public void removeMember(Long memberId) {
        memberRepository.deleteById(memberId);
    }
}
