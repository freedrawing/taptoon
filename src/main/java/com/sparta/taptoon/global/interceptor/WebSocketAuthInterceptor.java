package com.sparta.taptoon.global.interceptor;

import com.sparta.taptoon.domain.chat.service.ChatRoomMemberService;
import com.sparta.taptoon.domain.chat.service.ChatRoomService;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import com.sparta.taptoon.global.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
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
    private static final String NOTIFICATION_PATH_PREFIX = "/notifications/";
    public final MongoTemplate mongoTemplate;

    private final JwtUtil jwtUtil;
    private final ChatRoomMemberService chatRoomMemberService;
    private final ChatRoomService chatRoomService;

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
            log.warn("❌ WebSocket 인증 실패 - 이유: 토큰 없음");
            return false;
        }

        try {
            Long senderId = validateAndGetSenderId(token);
            String path = httpRequest.getRequestURI();

            if (path.startsWith(CHAT_PATH_PREFIX)) {
                String chatRoomId = extractChatRoomId(path);
                if(!chatRoomService.isMemberOfChatRoom(chatRoomId, senderId)){
                    log.warn("❌ WebSocket 연결 거부 - 사용자가 채팅방 멤버가 아님 (chatRoomId: {}, senderId: {})", chatRoomId, senderId);
                    return false;
                }
                attributes.put("chatRoomId", chatRoomId);
            }else if (path.startsWith(NOTIFICATION_PATH_PREFIX)){
                log.info("✅ Notification WebSocket 연결 요청 - senderId: {}", senderId);
            }else{
                log.warn("❌ WebSocket 인증 실패 - 알 수 없는 경로: {}", path);
                return false;
            }

            attributes.put("senderId", senderId);
            log.info("✅ WebSocket 인증 성공 - senderId: {}", senderId);
            return true;

        } catch (JwtException e) {
            log.error("❌ WebSocket 인증 실패 - 이유: JWT 토큰 검증 실패: {}", token, e);
            return false;
        } catch (NotFoundException e) {
            log.error("❌ WebSocket 인증 실패 - 이유: 채팅방 ID 파싱 실패 또는 존재하지 않음: {}", httpRequest.getRequestURI(), e);
            return false;
        } catch (Exception e) {
            log.error("❌ WebSocket 인증 실패 - 이유: 예상치 못한 오류: {}", token, e);
            return false;
        }
    }

    // Authorization 헤더에서 토큰 추출
    private String extractToken(HttpServletRequest request) {
        String tokenFromQuery = request.getParameter("token");
        if (tokenFromQuery != null) {
            // Bearer 접두사가 포함되어 있을 수 있으니 확인 후 제거
            if (tokenFromQuery.startsWith(BEARER_PREFIX)) {
                return tokenFromQuery.substring(BEARER_PREFIX.length());
            }
            return tokenFromQuery; // Bearer 접두사 없이 순수 JWT 토큰 반환
        }

        return null; // 토큰이 없으면 null 반환
    }

    // JWT 토큰 검증 및 senderId 추출
    private Long validateAndGetSenderId(String token) {
        String subject = jwtUtil.validateTokenAndGetClaims(token).getSubject();
        return Long.parseLong(subject);
    }

    // URL에서 chatRoomId 추출
    private String extractChatRoomId(String path) {
        try {
            String chatRoomIdStr = path.substring(path.indexOf(CHAT_PATH_PREFIX) + CHAT_PATH_PREFIX.length());
            return chatRoomIdStr;
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new NotFoundException(); // 채팅방 ID가 유효하지 않거나 경로에서 추출 불가
        }
    }

    // 사용자가 채팅방 멤버인지 확인
    private boolean isValidChatRoomMember(String chatRoomId, Long senderId) {
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
