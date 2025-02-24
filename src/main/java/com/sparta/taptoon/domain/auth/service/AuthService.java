package com.sparta.taptoon.domain.auth.service;

import com.sparta.taptoon.domain.auth.dto.request.LoginMemberRequest;
import com.sparta.taptoon.domain.auth.dto.request.OAuthDisconnectRequest;
import com.sparta.taptoon.domain.auth.dto.request.SignupMemberRequest;
import com.sparta.taptoon.domain.auth.dto.response.LoginMemberResponse;
import com.sparta.taptoon.domain.auth.dto.response.TokenInfo;
import com.sparta.taptoon.domain.auth.entity.RefreshToken;
import com.sparta.taptoon.domain.auth.repository.RefreshTokenRepository;
import com.sparta.taptoon.domain.member.dto.response.MemberResponse;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.dto.MemberDetail;
import com.sparta.taptoon.domain.member.enums.OAuthProvider;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
import com.sparta.taptoon.global.error.exception.InvalidRequestException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import com.sparta.taptoon.global.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public boolean checkEmailAlreadyExist(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }

    public MemberResponse signUp(SignupMemberRequest request) {
        Member member = request.toEntity(passwordEncoder);
        Member savedMember = memberRepository.save(member);
        return MemberResponse.from(savedMember);
    }

    public LoginMemberResponse login(LoginMemberRequest request, HttpServletRequest httpServletRequest) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());
        try{
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            TokenInfo accessToken = jwtUtil.generateAccessToken(authentication);
            TokenInfo refreshTokenInfo = jwtUtil.generateRefreshToken(authentication);
            Member member = ((MemberDetail) authentication.getPrincipal()).getMember();
            String deviceInfo = getMemberDeviceInfo(httpServletRequest);
            RefreshToken refreshToken = RefreshToken.builder()
                    .member(member)
                    .token(refreshTokenInfo.token())
                    .deviceInfo(deviceInfo)
                    .expiresAt(refreshTokenInfo.expiresAt())
                    .build();
            refreshTokenRepository.save(refreshToken);
            return new LoginMemberResponse(accessToken.token(),refreshTokenInfo.token(),accessToken.expiresAt());
        } catch (BadCredentialsException e) {
           throw new InvalidRequestException(ErrorCode.INVALID_CREDENTIALS);
        } catch (Exception e) {
            throw new AccessDeniedException(ErrorCode.LOGIN_NOT_ACCEPTABLE);
        }
    }

    @Transactional
    public TokenInfo issueAccessTokenByRefreshToken(String refreshTokenInfo) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenInfo)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TOKEN));
        if(refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new AccessDeniedException();
        }
        Member member = refreshToken.getMember();
        MemberDetail memberDetail = new MemberDetail(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                memberDetail,
                null,
                memberDetail.getAuthorities()
        );
        return jwtUtil.generateAccessToken(authentication);
    }

    @Transactional
    public void logout(Long memberId, HttpServletRequest httpServletRequest) {
        String deviceInfo = getMemberDeviceInfo(httpServletRequest);
        refreshTokenRepository.deleteByMemberIdAndDeviceInfo(memberId, deviceInfo);
    }

    @Transactional
    public void logoutAllDevice(Long memberId) {
        refreshTokenRepository.deleteAllByMemberId(memberId);
    }

    @Transactional
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

    private String getMemberDeviceInfo(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getHeader("User-Agent");
    }

}
