package com.sparta.taptoon.global.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.taptoon.domain.chat.dto.response.ChatRoomListResponse;
import com.sparta.taptoon.domain.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final ChatRoomService chatRoomService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = (Long) session.getAttributes().get("senderId");
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("✅ Notification WebSocket 연결 성공 - userId: {}", userId);
            sendInitialChatRoomData(userId, session);
        } else {
            log.warn("❌ Notification WebSocket 연결 실패 - userId 없음");
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 클라이언트에서 메시지를 받는 경우는 드물지만, 필요 시 처리 가능
        log.info("Received message from client: {}", message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("senderId");
        if (userId != null) {
            userSessions.remove(userId);
            log.info("✅ Notification WebSocket 연결 종료 - userId: {}, 이유: {}", userId, status);
        }
    }

    // 초기 채팅방 데이터 전송
    private void sendInitialChatRoomData(Long userId, WebSocketSession session) throws Exception {
        List<ChatRoomListResponse> chatRooms = chatRoomService.getChatRooms(userId); // DTO 반환
        Map<String, Object> initialData = new HashMap<>();
        initialData.put("type", "initial");
        initialData.put("chatRooms", chatRooms);

        String jsonMessage = objectMapper.writeValueAsString(initialData);
        session.sendMessage(new TextMessage(jsonMessage));
        log.info("✅ 초기 채팅방 데이터 전송 - userId: {}", userId);
    }

    // 알림 전송 메서드 (외부에서 호출 가능)
    public void sendNotification(Long userId, Map<String, Object> notification) throws IOException {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            String jsonMessage = objectMapper.writeValueAsString(notification);
            session.sendMessage(new TextMessage(jsonMessage));
            log.info("✅ 알림 전송 성공 - userId: {}, 메시지: {}", userId, jsonMessage);
        }
    }
}
