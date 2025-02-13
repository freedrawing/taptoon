package com.sparta.taptoon.global.util;

import com.sparta.taptoon.domain.member.entity.MemberDetail;
import com.sparta.taptoon.global.error.exception.InvalidRequestException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final String secretKey;
    private static final String BEARER_PREFIX = "Bearer ";
    private final long ACCESS_TOKEN_EXPIRE_TIME = 60 * 60 * 1000L; // 60분
    private final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L; // 1주일

    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    public String generateAccessToken(Authentication authentication) {
        return generateToken(ACCESS_TOKEN_EXPIRE_TIME,authentication);
    }

    public String generateRefreshToken(Authentication authentication) {
        return generateToken(REFRESH_TOKEN_EXPIRE_TIME,authentication);
    }

    // 토큰 생성
    private String generateToken(long expireTime, Authentication authentication) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireTime);
        MemberDetail memberDetail = (MemberDetail) authentication.getPrincipal();

        return BEARER_PREFIX + Jwts.builder()
                .setSubject(String.valueOf(memberDetail.getId()))
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, signatureAlgorithm)
                .compact();
    }

    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        throw new NotFoundException();//exception 처리필요
    }

    // 토큰 유효성 검증
    public void validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
        } catch (Exception e) {
            throw new InvalidRequestException(); //exception 처리필요
        }
    }
    public Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
    }
}