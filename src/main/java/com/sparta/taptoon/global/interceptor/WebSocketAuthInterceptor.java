package com.sparta.taptoon.global.interceptor;

import com.sparta.taptoon.domain.member.dto.MemberDetail;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import com.sparta.taptoon.global.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String token = httpRequest.getHeader("Authorization");

            if (token == null || !token.startsWith("Bearer ")) {
                log.warn("❌ WebSocket 요청에 Authorization 헤더 없음");
                return false; // 인증 실패
            }

            try {
                // "Bearer " 제거 후 실제 토큰만 남기기
                token = token.substring(7);

                // JWT 검증
                jwtUtil.validateTokenAndGetClaims(token);
                Long senderId = Long.parseLong(jwtUtil.validateTokenAndGetClaims(token).getSubject());

                // Member 정보 조회
                Member member = memberRepository.findById(senderId)
                        .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

                // Authentication 객체 생성 및 SecurityContext에 저장
                MemberDetail memberDetail = new MemberDetail(member);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        memberDetail, null, memberDetail.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // WebSocketSession attributes에 사용자 ID 저장
                attributes.put("senderId", senderId);
                log.info("✅ WebSocket 인증 성공 - senderId: {}", senderId);
                return true;

            } catch (Exception e) {
                log.error("❌ WebSocket JWT 인증 실패", e);
                return false;
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {

    }
}
