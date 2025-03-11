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
        String header = request.getHeader(AUTHORIZATION_HEADER);
        String requestURI = request.getRequestURI();
        if (isWhiteList(requestURI)) {
            chain.doFilter(request, response);
            return;
        }
        // 헤더가 없거나 비어있거나 "null"인 경우 인증 필요 경로면 차단
        if (header == null || header.isEmpty() || header.equals("null")) {
            log.info("No authorization header for protected resource");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
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

    private boolean isWhiteList(String requestURI) {
        return requestURI.startsWith("/api/auth/") ||
                requestURI.startsWith("/v3/api-docs/") ||
                requestURI.startsWith("/swagger-ui/") ||
                requestURI.equals("/swagger-ui.html");
    }
}