package com.sparta.taptoon.domain.chat.controller;

import com.sparta.taptoon.domain.chat.dto.request.CreateChatRoomRequest;
import com.sparta.taptoon.domain.chat.dto.response.ChatRoomListResponse;
import com.sparta.taptoon.domain.chat.dto.response.ChatRoomResponse;
import com.sparta.taptoon.domain.chat.service.ChatRoomService;
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
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @Operation(summary = "채팅방 개설")
    @PostMapping("/chat-room")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(
            @AuthenticationPrincipal MemberDetail memberDetail, // 요청한 유저
            @Valid @RequestBody CreateChatRoomRequest request) { // 대화 상대 유저 ID

        ChatRoomResponse response = chatRoomService.createChatRoom(memberDetail.getId(), request);
        return ApiResponse.created(response);
    }

    @Operation(summary = "채팅방 목록 조회")
    @GetMapping("/chat-rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomListResponse>>> getChatRooms(
            @AuthenticationPrincipal MemberDetail memberDetail) {

        List<ChatRoomListResponse> chatRooms = chatRoomService.getChatRooms(memberDetail.getId());
        return ApiResponse.success(chatRooms);
    }

    @Operation(summary = "채팅방 삭제")
    @DeleteMapping("/chat-room/{chatRoomId}")
    public ResponseEntity<ApiResponse<Void>> deleteChatRoom(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long chatRoomId) {

        chatRoomService.deleteChatRoom(memberDetail.getId(), chatRoomId);
        return ApiResponse.noContent();
    }
}
