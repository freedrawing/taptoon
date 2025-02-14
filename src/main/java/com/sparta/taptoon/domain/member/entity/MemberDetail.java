package com.sparta.taptoon.domain.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@RequiredArgsConstructor
public class MemberDetail implements UserDetails {
    private final Member member;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("GRADE_" + member.getGrade().name()));
    }

    public Long getId() {
        return member.getId();
    }

    public String getEmail() {
        return member.getEmail();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getName();
    }

    @Override
    public boolean isAccountNonExpired() {//계정 만료 여부(멤버십 기간 만료), false: AccountExpiredException 발생
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {//계정 잠김 여부(비밀번호 시도 회수 초과), false: LockedException  발생
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {//계정 만료 여부(토큰 사용 기간 만료), false: CredentialsExpiredException  발생
        return true;
    }

    @Override
    public boolean isEnabled() {//계정 활성화 여부(탈퇴), false: DisabledException  발생
        return !member.getIsDeleted();
    }
}
