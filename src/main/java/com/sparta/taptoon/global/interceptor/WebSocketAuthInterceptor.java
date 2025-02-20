package com.sparta.taptoon.global.interceptor;

import com.sparta.taptoon.domain.chat.service.ChatRoomMemberService;
import com.sparta.taptoon.global.util.JwtUtil;
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

    private final JwtUtil jwtUtil;
    private final ChatRoomMemberService chatRoomMemberService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String token = httpRequest.getHeader("Authorization");

            if (token == null || !token.startsWith("Bearer ")) {
                log.warn("❌ WebSocket 요청에 Authorization 헤더 없음");
                return false;
            }

            try {
                token = token.substring(7);
                Long senderId = Long.parseLong(jwtUtil.validateTokenAndGetClaims(token).getSubject());

                // URL에서 chatRoomId 추출
                String path = httpRequest.getRequestURI();
                Long chatRoomId = extractChatRoomIdFromPath(path);

                // 사용자가 채팅방 멤버인지 검증
                if (!chatRoomMemberService.isMemberOfChatRoom(chatRoomId, senderId)) {
                    log.warn("❌ WebSocket 연결 거부 - 사용자가 채팅방 멤버가 아님 (chatRoomId: {}, senderId: {})", chatRoomId, senderId);
                    return false;
                }

                attributes.put("senderId", senderId);
                attributes.put("chatRoomId", chatRoomId); // ✅ 추가
                log.info("✅ WebSocket 인증 성공 - senderId: {}, chatRoomId: {}", senderId, chatRoomId);
                return true;

            } catch (Exception e) {
                log.error("❌ WebSocket JWT 인증 실패", e);
                return false;
            }
        }
        return false;
    }

    private Long extractChatRoomIdFromPath(String path) {
        String[] parts = path.split("/");
        try {
            return Long.parseLong(parts[parts.length - 1]); // 예: "/ws/chat/5" → chatRoomId = 5
        } catch (NumberFormatException e) {
            log.error("❌ 채팅방 ID 추출 실패 (path: {})", path);
            return null;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
