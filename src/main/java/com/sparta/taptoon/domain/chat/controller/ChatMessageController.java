package com.sparta.taptoon.domain.chat.controller;

import com.sparta.taptoon.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

//    /**
//     * 메시지 전송
//     */
//    @MessageMapping("/chat/message") // "/pub/chat/message"로 들어오는 메시지 처리
//    public void sendMessage(SendChatMessageRequest request) {
//        chatMessageService.sendMessage(request);
//    }

//    /**
//     * 특정 채팅방의 메시지 목록 조회
//     */
//    @GetMapping("/{chatRoomId}/messages")
//    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(@PathVariable Long chatRoomId) {
//        List<ChatMessageResponse> messages = chatMessageService.getChatMessages(chatRoomId);
//        return ApiResponse.success(messages);
//    }
}
