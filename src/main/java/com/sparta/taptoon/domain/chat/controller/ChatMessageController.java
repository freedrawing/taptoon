package com.sparta.taptoon.domain.chat.controller;

import com.sparta.taptoon.domain.chat.dto.request.SendChatMessageRequest;
import com.sparta.taptoon.domain.chat.dto.response.ChatCombinedMessageResponse;
import com.sparta.taptoon.domain.chat.dto.response.ChatMessageResponse;
import com.sparta.taptoon.domain.chat.service.ChatMessageService;
import com.sparta.taptoon.domain.image.service.ImageService;
import com.sparta.taptoon.domain.member.dto.MemberDetail;
import com.sparta.taptoon.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final ImageService imageService;

    @Operation(summary = "메시지 전송")
    @PostMapping("/{chatRoomId}/message")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @AuthenticationPrincipal MemberDetail memberDetail, // JWT에서 유저 정보 가져옴
            @PathVariable String chatRoomId,
            @Valid @RequestBody SendChatMessageRequest request) {

        ChatMessageResponse response = chatMessageService.sendMessage(memberDetail.getId(), chatRoomId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "채팅 메시지 조회 + 읽음 처리(텍스트 + 이미지")
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatCombinedMessageResponse>>> getChatMessages(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable String chatRoomId){

        List<ChatCombinedMessageResponse> messages = chatMessageService.getChatMessages(memberDetail.getId(), chatRoomId);
        return ApiResponse.success(messages);
    }

    @Operation(summary = "읽지 않은 메시지 수 조회")
    @GetMapping("/{chatRoomId}/unread")
    public ResponseEntity<ApiResponse<Integer>> getUnreadCount(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable String chatRoomId) {
        int unreadCount = chatMessageService.calculateUnreadCount(chatRoomId, memberDetail.getId());
        return ApiResponse.success(unreadCount);
    }

//    @Operation(summary = "채팅 이미지 업로드를 위한 pre-signed URL 생성")
//    @PostMapping("/{chatRoomId}/image-upload")
//    public ResponseEntity<ApiResponse<String>> getImagePresignedUrl(
//            @AuthenticationPrincipal MemberDetail memberDetail,
//            @PathVariable Long chatRoomId,
//            @RequestParam String folderPath,
//            @RequestParam String fileName) {
//        String presignedUrl = imageService.generatePresignedUrl(folderPath, chatRoomId, memberDetail.getId(),fileName);
//        return ApiResponse.success(presignedUrl);
//    }

//    @Operation(summary = "채팅 이미지 메시지 전송")
//    @PostMapping("/{chatRoomId}/image-message")
//    public ResponseEntity<ApiResponse<ChatImageMessageResponse>> sendImageMessage(
//            @AuthenticationPrincipal MemberDetail memberDetail,
//            @PathVariable Long chatRoomId,
//            @Valid @RequestBody SendChatImageMessageRequest request) {
//        ChatImageMessageResponse response = chatMessageService.sendImageMessage(memberDetail.getId(), chatRoomId, request);
//        return ApiResponse.success(response);
//    }
}
