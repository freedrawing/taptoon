package com.sparta.taptoon.global.handler;

import com.sparta.taptoon.domain.chat.dto.response.ChatRoomListResponse;
import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.chat.service.ChatMessageService;
import com.sparta.taptoon.domain.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    public void notifyNewMessage(String chatRoomId, Long senderId, String message) {
        ChatRoom chatRoom = chatRoomService.findChatRoom(chatRoomId);
        List<Long> memberIds = chatRoom.getMemberIds();

        memberIds.forEach(memberId -> {
            int unreadCount = chatMessageService.calculateUnreadCount(chatRoomId, memberId);
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "message");
            notification.put("chatRoomId", chatRoomId);
            notification.put("senderId", senderId);
            notification.put("message", message);
            notification.put("unread_count", unreadCount);
            notification.put("timestamp", System.currentTimeMillis());

            ChatRoomListResponse chatRoomResponse = ChatRoomListResponse.from(chatRoom, message,
                    String.valueOf(System.currentTimeMillis()), unreadCount);
            notification.put("chatRoom", chatRoomResponse);

            try {
                notificationWebSocketHandler.sendNotification(memberId, notification);
            } catch (IOException e) {
                log.error("Failed to send notification to memberId: {}", memberId, e);
            }
        });
        log.info("✅ notifyNewMessage - chatRoomId: {}, senderId: {}", chatRoomId, senderId);
    }
}
