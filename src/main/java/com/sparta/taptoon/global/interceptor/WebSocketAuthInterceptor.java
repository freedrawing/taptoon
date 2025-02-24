package com.sparta.taptoon.global.interceptor;

import com.sparta.taptoon.domain.chat.service.ChatRoomMemberService;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import com.sparta.taptoon.global.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CHAT_PATH_PREFIX = "/ws/chat/";

    private final JwtUtil jwtUtil;
    private final ChatRoomMemberService chatRoomMemberService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            log.warn("❌ WebSocket 인증 실패 - 이유: {}", "ServletServerHttpRequest가 아님");
            return false;
        }

        HttpServletRequest httpRequest = servletRequest.getServletRequest();
        String token = extractToken(httpRequest);
        if (token == null) {
            log.warn("❌ WebSocket 인증 실패 - 이유: {}", "Authorization 헤더 없음 또는 Bearer 형식이 아님");
            return false;
        }

        try {
            Long senderId = validateAndGetSenderId(token);
            Long chatRoomId = extractChatRoomId(httpRequest.getRequestURI());

            if (!isValidChatRoomMember(chatRoomId, senderId)) {
                log.warn("❌ WebSocket 연결 거부 - 사용자가 채팅방 멤버가 아님 (chatRoomId: {}, senderId: {})", chatRoomId, senderId);
                return false;
            }

            attributes.put("senderId", senderId);
            attributes.put("chatRoomId", chatRoomId);
            log.info("✅ WebSocket 인증 성공 - senderId: {}, chatRoomId: {}", senderId, chatRoomId);
            return true;

        } catch (JwtException e) {
            log.error("❌ WebSocket 인증 실패 - 이유: {}", "JWT 토큰 검증 실패: " + token, e);
            return false;
        } catch (NotFoundException e) {
            log.error("❌ WebSocket 인증 실패 - 이유: {}", "JWT 토큰 검증 실패: ", "채팅방 ID 파싱 실패 또는 존재하지 않음: " + httpRequest.getRequestURI(), e);
            return false;
        } catch (Exception e) {
            log.error("❌ WebSocket 인증 실패 - 이유: {}", "JWT 토큰 검증 실패: ", "예상치 못한 오류: " + token, e);
            return false;
        }
    }

    // Authorization 헤더에서 토큰 추출
    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader(AUTHORIZATION_HEADER);
        if (token == null || !token.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return token.substring(BEARER_PREFIX.length());
    }

    // JWT 토큰 검증 및 senderId 추출
    private Long validateAndGetSenderId(String token) {
        String subject = jwtUtil.validateTokenAndGetClaims(token).getSubject();
        return Long.parseLong(subject);
    }

    // URL에서 chatRoomId 추출
    private Long extractChatRoomId(String path) {
        try {
            String chatRoomIdStr = path.substring(path.indexOf(CHAT_PATH_PREFIX) + CHAT_PATH_PREFIX.length());
            return Long.parseLong(chatRoomIdStr);
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new NotFoundException(); // 채팅방 ID가 유효하지 않거나 경로에서 추출 불가
        }
    }

    // 사용자가 채팅방 멤버인지 확인
    private boolean isValidChatRoomMember(Long chatRoomId, Long senderId) {
        return chatRoomMemberService.isMemberOfChatRoom(chatRoomId, senderId);
    }

    /**
     * 핸드셰이크 후 실행 (현재 사용되지 않음)
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 필요 시 후속 처리 추가 가능
    }
}
