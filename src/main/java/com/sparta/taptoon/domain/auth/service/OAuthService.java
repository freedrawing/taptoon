package com.sparta.taptoon.domain.auth.service;

import com.sparta.taptoon.domain.auth.dto.OAuthMemberInfo;
import com.sparta.taptoon.domain.auth.dto.response.LoginMemberResponse;
import com.sparta.taptoon.domain.auth.dto.response.TokenInfo;
import com.sparta.taptoon.domain.auth.entity.RefreshToken;
import com.sparta.taptoon.domain.auth.repository.RefreshTokenRepository;
import com.sparta.taptoon.domain.member.dto.MemberDetail;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.enums.OAuthProvider;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import com.sparta.taptoon.global.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public LoginMemberResponse processLogin(OAuthProvider provider, OAuthMemberInfo memberInfo, HttpServletRequest request) {
        Member member = memberRepository.findByProviderAndProviderId(provider, memberInfo.getId())
                .orElseGet(() -> registerMember(provider, memberInfo));

        Authentication authentication = createAuthentication(member);
        TokenInfo accessToken = jwtUtil.generateAccessToken(authentication);
        TokenInfo refreshTokenInfo = jwtUtil.generateRefreshToken(authentication);

        saveRefreshToken(member, refreshTokenInfo, request);

        return new LoginMemberResponse(
                accessToken.token(),
                refreshTokenInfo.token(),
                accessToken.expiresAt()
        );
    }
    private Member registerMember(OAuthProvider provider, OAuthMemberInfo memberInfo) {
        Member member = Member.builder()
                .provider(provider)
                .providerId(memberInfo.getId())
                .email(memberInfo.getEmail() != null ? memberInfo.getEmail() : "") // 이메일이 없는 경우 빈 문자열
                .password("")
                .name(memberInfo.getName())
                .build();
        return memberRepository.save(member);
    }

    private Authentication createAuthentication(Member member) {
        MemberDetail memberDetail = new MemberDetail(member);
        return new UsernamePasswordAuthenticationToken(
                memberDetail,
                null,
                memberDetail.getAuthorities()
        );
    }

    private void saveRefreshToken(Member member, TokenInfo refreshTokenInfo, HttpServletRequest request) {
        String deviceInfo = getMemberDeviceInfo(request);
        RefreshToken refreshToken = RefreshToken.builder()
                .member(member)
                .token(refreshTokenInfo.token())
                .deviceInfo(deviceInfo)
                .expiresAt(refreshTokenInfo.expiresAt())
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    private String getMemberDeviceInfo(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    @org.springframework.transaction.annotation.Transactional
    public void disconnectOAuthMember(String providerId, String provider) {
        OAuthProvider oAuthProvider = OAuthProvider.valueOf(provider);
        Member member = memberRepository.findByProviderAndProviderId(oAuthProvider, providerId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        member.disconnectOAuthInfo();
        if(member.getEmail() == null || member.getPassword()== null) {
            log.info("소셜 로그인 연동 해제로 인한 정보 부족으로 탈퇴 진행, memberId = {}",member.getId());
            member.withdrawMember();
        }
    }
}
