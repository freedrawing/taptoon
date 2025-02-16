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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends GenericFilterBean {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String header = httpRequest.getHeader(AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(header)) {
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
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }

        chain.doFilter(request, response);
    }
}