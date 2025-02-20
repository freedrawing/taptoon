package com.sparta.taptoon.domain.member.service;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.dto.MemberDetail;
import com.sparta.taptoon.domain.member.enums.OAuthProvider;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String userIdentification) throws UsernameNotFoundException {
        Member member = null;
        // 이메일 형식인지 확인
        if (isValidEmail(userIdentification)) {
            log.info("이메일로 로그인함! email: {}", userIdentification);
            member = memberRepository.findByEmail(userIdentification)
                    .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다."));
        } else {
            log.info("소셜 로그인함! providerId: {}", userIdentification);
            member = memberRepository.findByProviderId(userIdentification)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        }
        if(member.getIsDeleted()) throw new AccessDeniedException(ErrorCode.MEMBER_DELETED);
        return new MemberDetail(member);
    }
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
}
