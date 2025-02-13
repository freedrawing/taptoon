package com.sparta.taptoon.domain.chat.controller;

import com.sparta.taptoon.domain.chat.dto.request.SendChatMessageRequest;
import com.sparta.taptoon.domain.chat.dto.response.ChatMessageResponse;
import com.sparta.taptoon.domain.chat.service.ChatMessageService;
import com.sparta.taptoon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat/messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    /**
     * 메시지 전송
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(@RequestBody SendChatMessageRequest request) {
        ChatMessageResponse response = chatMessageService.sendMessage(request);
        return ApiResponse.success(response);
    }

    /**
     * 특정 채팅방의 메시지 목록 조회
     */
    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(@PathVariable Long chatRoomId) {
        List<ChatMessageResponse> messages = chatMessageService.getMessagesByChatRoom(chatRoomId);
        return ApiResponse.success(messages);
    }
}
