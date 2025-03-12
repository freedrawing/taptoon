package com.sparta.taptoon.global.config;

// JwtFilter.java

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.dto.MemberDetail;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import com.sparta.taptoon.global.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // WebSocket 핸드셰이크 요청인지 확인
        if (request.getHeader("Upgrade") != null && "websocket".equalsIgnoreCase(request.getHeader("Upgrade"))) {
            log.info("웹소켓 handshake 요청을 감지했는데, URI: {}, 필터 검사 생략", request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }
        String header = request.getHeader(AUTHORIZATION_HEADER);
        log.info("uri: {} header: {}", request.getRequestURI(),header);
        if (!StringUtils.hasText(header)) {
            log.info("토큰 정보가 없지만, security 에서 보증했기에 인증 생략합니다.");
            chain.doFilter(request, response);
            return;
        }
        try {
            String token = jwtUtil.substringToken(header);
            Claims claims = jwtUtil.validateTokenAndGetClaims(token);
            Member member = memberRepository.findById(Long.parseLong(claims.getSubject())).orElseThrow(NotFoundException::new);
            MemberDetail memberDetail = new MemberDetail(member);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    memberDetail,
                    null,
                    memberDetail.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }
        chain.doFilter(request, response);
    }
}