package com.sparta.taptoon.domain.chat.controller;

import com.sparta.taptoon.domain.chat.dto.request.SendChatMessageRequest;
import com.sparta.taptoon.domain.chat.dto.response.ChatMessageResponse;
import com.sparta.taptoon.domain.chat.service.ChatMessageService;
import com.sparta.taptoon.domain.member.entity.MemberDetail;
import com.sparta.taptoon.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @Operation(summary = "메시지 전송")
    @PostMapping("/{chatRoomId}/message")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @AuthenticationPrincipal MemberDetail memberDetail, // JWT에서 유저 정보 가져옴
            @PathVariable Long chatRoomId,
            @RequestBody SendChatMessageRequest request) {

        ChatMessageResponse response = chatMessageService.sendMessage(memberDetail.getId(), request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatMessages(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long chatRoomId){

        List<ChatMessageResponse> messages = chatMessageService.getChatMessages(memberDetail.getId(), chatRoomId);
        return ApiResponse.success(messages);
    }
}
