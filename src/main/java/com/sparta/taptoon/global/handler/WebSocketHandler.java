package com.sparta.taptoon.global.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.taptoon.domain.chat.dto.request.SendChatMessageRequest;
import com.sparta.taptoon.domain.chat.service.ChatMessageService;
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
import com.sparta.taptoon.global.error.exception.InvalidRequestException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private static final String CHAT_ROOM_ID_PATH_INDEX = "/chat/";

    private final ChatMessageService chatMessageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 채팅방별 WebSocket 세션 관리
    private static final Map<String, Set<WebSocketSession>> chatRoomSessions = new ConcurrentHashMap<>();

    /**
     * WebSocket 연결이 성공하면 실행
     * - 채팅방 ID를 추출하여 세션 목록에 추가
     * - JWT 인증된 사용자만 연결 유지
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String chatRoomId = extractChatRoomId(session);
        Long senderId = getSenderIdFromSession(session);

        if (chatRoomId == null || senderId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        chatRoomSessions.computeIfAbsent(chatRoomId, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("✅ 채팅방 {} 에 세션 {} 추가 완료 (사용자: {})", chatRoomId, session.getId(), senderId);

    }

    /**
     *클라이언트가 메시지를 보내면 실행
     *
     * objectMapper.readTree(payload)를 이용해 JSON 형식으로 메시지를 파싱
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.info("📩 받은 메시지: {}", payload);

        try {
            MessagePayload messagePayload = parseMessagePayload(payload);
            String chatRoomId = extractChatRoomId(session);

            chatMessageService.sendMessage(messagePayload.senderId(), chatRoomId,
                    new SendChatMessageRequest(messagePayload.message()));
        } catch (NotFoundException e) {
            log.error("❌ 채팅방 또는 사용자를 찾을 수 없음: {}", payload, e);
            sendErrorMessage(session, "Chat room or user not found");
        } catch (AccessDeniedException e) {
            log.error("❌ 채팅방 접근 권한 없음: {}", payload, e);
            sendErrorMessage(session, "Access denied to chat room");
        } catch (InvalidRequestException e) {
            log.error("❌ 유효하지 않은 메시지 요청: {}", payload, e);
            sendErrorMessage(session, "Invalid message request");
        } catch (Exception e) {
            log.error("❌ WebSocket 메시지 처리 중 오류 발생: {}", payload, e);
            sendErrorMessage(session, "Server error processing message");
        }
    }

    /**
     *클라이언트가 WebSocket 연결을 종료하면 실행
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        chatRoomSessions.values().forEach(sessions -> sessions.remove(session));
        log.info(" WebSocket 연결 종료: {}", session.getId());
    }

    /**
     * Redis에서 수신한 메시지를 WebSocket을 통해 해당 채팅방의 모든 클라이언트에게 전송
     */
    public void broadcastMessage(String chatRoomId, String message) throws Exception {
        log.info("📤 WebSocket을 통해 메시지 브로드캐스트 - chatRoomId: {}, 메시지: {}", chatRoomId, message);

        Set<WebSocketSession> sessions = chatRoomSessions.getOrDefault(chatRoomId, Collections.emptySet());
        JsonNode jsonNode = objectMapper.readTree(message);
        String messageId = jsonNode.get("id").asText();
        Long senderIdFromMessage = jsonNode.get("sender_id").asLong();

        for (WebSocketSession session : sessions) {
            Long sessionSenderId = getSenderIdFromSession(session);
            if (sessionSenderId != null && sessionSenderId.equals(senderIdFromMessage)) {
                chatMessageService.updateLastReadMessage(chatRoomId, sessionSenderId, messageId);
            }
        }

        sendMessageToChatRoom(chatRoomId, message);
    }

    /**
     * 특정 채팅방의 모든 WebSocket 세션을 찾아 메시지를 전송
     */
    private void sendMessageToChatRoom(String chatRoomId, String message) throws Exception {
        Set<WebSocketSession> sessions = chatRoomSessions.getOrDefault(chatRoomId, Collections.emptySet());
        log.info("📤 채팅방 {} 에 연결된 세션 수: {}", chatRoomId, sessions.size());

        for (WebSocketSession session : sessions) {
            log.info("📤 메시지 전송 중 -> 세션 ID: {}", session.getId());
            session.sendMessage(new TextMessage(message));
        }
        log.info("✅ 채팅방 {} 에 {} 개 세션에 메시지 전송 완료", chatRoomId, sessions.size());
    }

    /**
     * WebSocket의 URL에서 채팅방 ID를 추출하는 메서드
     */
    private String extractChatRoomId(WebSocketSession session) {
        try {
            String path = session.getUri().getPath();
            String chatRoomIdStr = path.substring(path.indexOf(CHAT_ROOM_ID_PATH_INDEX) + CHAT_ROOM_ID_PATH_INDEX.length());
            return chatRoomIdStr;
        } catch (Exception e) {
            log.warn("❌ WebSocket URL에서 채팅방 ID 추출 실패: {}", session.getUri().getPath(), e);
            return null;
        }
    }

    /**
     * JWT 토큰에서 senderId 추출하는 메서드
     */
    private Long getSenderIdFromSession(WebSocketSession session) {
        Object senderId = session.getAttributes().get("senderId");
        return senderId != null ? (Long) senderId : null;
    }

    /**
     * WebSocket 오류 발생 시 클라이언트에 메시지 전송
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        if (!session.isOpen()) {
            log.warn("❌ 세션이 닫혀 있어 오류 메시지 전송 불가: {}", session.getId());
            return;
        }
        try {
            String jsonError = objectMapper.writeValueAsString(Map.of("error", errorMessage));
            session.sendMessage(new TextMessage(jsonError));
        } catch (IOException e) {
            log.error("❌ WebSocket 오류 메시지 전송 실패: {}", errorMessage, e);
        }
    }

    // JSON 메시지를 파싱하여 필요한 데이터를 추출
    private MessagePayload parseMessagePayload(String payload) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(payload);
        Long senderId = jsonNode.get("senderId").asLong();
        String message = jsonNode.get("message").asText();
        return new MessagePayload(senderId, message);
    }

    // 메시지 페이로드 데이터 홀더
    private record MessagePayload(Long senderId, String message) {}
}

