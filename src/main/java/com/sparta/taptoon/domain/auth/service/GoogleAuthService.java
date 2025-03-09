package com.sparta.taptoon.domain.auth.service;

import com.sparta.taptoon.domain.auth.dto.response.LoginMemberResponse;
import com.sparta.taptoon.domain.auth.dto.response.TokenInfo;
import com.sparta.taptoon.domain.member.dto.MemberDetail;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.enums.OAuthProvider;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthService {
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    public LoginMemberResponse googleLogin(OAuth2User oauth2User) {
        log.info("Google 의 모든 attributes: {}", oauth2User.getAttributes());
        String providerId = oauth2User.getAttribute("sub");  // 구글은 sub가 id임!
        String name = oauth2User.getAttribute("name");
        log.info("구글 로그인 유저 id: {}, 이름: {}", providerId, name);
        Member member = memberRepository.findByProviderAndProviderId(OAuthProvider.GOOGLE, providerId)
                .orElseGet(() -> registerGoogleMember(providerId, name));

        Authentication authentication = createAuthentication(member);
        TokenInfo accessToken = jwtUtil.generateAccessToken(authentication);
        TokenInfo refreshTokenInfo = jwtUtil.generateRefreshToken(authentication);

        return new LoginMemberResponse(
                accessToken.token(),
                refreshTokenInfo.token(),
                accessToken.expiresAt()
        );
    }

    private Member registerGoogleMember(String providerId, String name) {
        Member member = Member.builder()
                .provider(OAuthProvider.GOOGLE)
                .providerId(providerId)
                .name(name)
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
}
