package com.sparta.taptoon.global.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.taptoon.domain.chat.dto.request.SendChatMessageRequest;
import com.sparta.taptoon.domain.chat.dto.response.ChatMessageResponse;
import com.sparta.taptoon.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatMessageService chatMessageService;

    // 채팅방별 Websocket 세션 저장
    private final Map<Long, Set<WebSocketSession>> chatRooms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long roomId = getRoomId(session);
        chatRooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
        System.out.println("새로운 WebSocket 연결: " + session.getId() + " (채팅방: " + roomId + ")");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long roomId = getRoomId(session);
        System.out.println("메시지 수신 (채팅방 " + roomId + "): " + message.getPayload());

        // JSON 데이터를 DTO로 변환
        SendChatMessageRequest request = objectMapper.readValue(message.getPayload(), SendChatMessageRequest.class);

        // DB에 메시지 저장
        ChatMessageResponse savedMessage = chatMessageService.sendMessage(request);

        // 해당 채팅방에 속한 사용자들에게 메시지 전송
        TextMessage responseMessage = new TextMessage(objectMapper.writeValueAsString(savedMessage));
        for (WebSocketSession s : chatRooms.getOrDefault(roomId, Set.of())) {
            if (s.isOpen() && !s.getId().equals(session.getId())) { // 자신에게 중복 전송 방지
                s.sendMessage(responseMessage);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long roomId = getRoomId(session);
        chatRooms.getOrDefault(roomId, Set.of()).remove(session);
        System.out.println("WebSocket 연결 종료: " + session.getId() + " (채팅방: " + roomId + ")");
    }

    private Long getRoomId(WebSocketSession session) {
        String path = session.getUri().getPath();
        String roomIdStr = path.substring(path.lastIndexOf("/") + 1);
        return Long.parseLong(roomIdStr);
    }
}

