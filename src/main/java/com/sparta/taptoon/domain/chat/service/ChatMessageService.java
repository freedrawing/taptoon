package com.sparta.taptoon.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.taptoon.domain.chat.dto.request.SendChatImageMessageRequest;
import com.sparta.taptoon.domain.chat.dto.request.SendChatMessageRequest;
import com.sparta.taptoon.domain.chat.dto.response.ChatCombinedMessageResponse;
import com.sparta.taptoon.domain.chat.dto.response.ChatImageMessageResponse;
import com.sparta.taptoon.domain.chat.dto.response.ChatMessageResponse;
import com.sparta.taptoon.domain.chat.entity.ChatImageMessage;
import com.sparta.taptoon.domain.chat.entity.ChatMessage;
import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.chat.repository.ChatImageMessageRepository;
import com.sparta.taptoon.domain.chat.repository.ChatMessageRepository;
import com.sparta.taptoon.domain.chat.repository.ChatRoomMemberRepository;
import com.sparta.taptoon.domain.chat.repository.ChatRoomRepository;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.common.enums.ImageStatus;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import com.sparta.taptoon.global.redis.RedisPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private static final String LAST_READ_MESSAGE_KEY_TEMPLATE = "chat:room:%d:user:%d";

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MemberRepository memberRepository;
    private final RedisPublisher redisPublisher;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final SlackAlarmService slackAlarmService;
    private final ChatImageMessageRepository chatImageMessageRepository;

    /**
     * 메시지 전송 & 저장, Redis와 Slack으로 알림을 발행합니다.
     *
     * @param senderId 메시지를 보내는 사용자의 ID
     * @param chatRoomId 메시지가 전송될 채팅방 ID
     * @param request 메시지 내용이 담긴 요청
     * @return 저장된 메시지의 응답 DTO
     */
    @Transactional
    public ChatMessageResponse sendMessage(Long senderId, Long chatRoomId, SendChatMessageRequest request) {
        ChatRoom chatRoom = findChatRoom(chatRoomId);
        Member sender = findMember(senderId);
        validateChatRoomMembership(chatRoom, sender);

        ChatMessage chatMessage = saveChatMessage(request, chatRoom, sender);
        ChatMessageResponse response = ChatMessageResponse.from(chatMessage);

        publishMessage(chatRoom.getId(), response, sender, request.message());
        return response;
    }

    @Transactional
    public ChatImageMessageResponse sendImageMessage(Long senderId, Long chatRoomId, SendChatImageMessageRequest request) {
        ChatRoom chatRoom = findChatRoom(chatRoomId);
        Member sender = findMember(senderId);
        validateChatRoomMembership(chatRoom, sender);

        ChatImageMessage imageMessage = chatImageMessageRepository.findByIdAndChatRoom(request.imageMessageId(), chatRoom)
                .orElseThrow(() -> new IllegalArgumentException("이미지 메시지를 찾을 수 없습니다."));

        if (!imageMessage.getSender().getId().equals(senderId)) {
            throw new AccessDeniedException("본인이 업로드한 이미지만 전송할 수 있습니다.");
        }
        if (imageMessage.getStatus() != ImageStatus.UPLOADED) {
            throw new AccessDeniedException("이미 전송된 이미지입니다.");
        }

        imageMessage.updateStatus(ImageStatus.COMPLETED);
        imageMessage.setUnreadCount(chatRoom.getMemberCount() - 1); // 전송 시 읽지 않은 멤버 수 설정
        chatImageMessageRepository.save(imageMessage);

        ChatImageMessageResponse response = ChatImageMessageResponse.from(imageMessage);
        publishImage(chatRoom.getId(), response, sender, "이미지 메시지 전송");
        return response;
    }

    /**
     * 채팅방의 모든 메시지를 조회하고, 읽지 않은 메시지를 읽음 처리.
     *
     * @param memberId 메시지를 조회하는 사용자의 ID
     * @param chatRoomId 조회할 채팅방 ID
     * @return 채팅방의 메시지 목록
     */
    @Transactional
    public List<ChatCombinedMessageResponse> getChatMessages(Long memberId, Long chatRoomId) {
        ChatRoom chatRoom = findChatRoom(chatRoomId);
        Member member = findMember(memberId);
        validateChatRoomMembership(chatRoom, member);

        Long lastReadMessageId = getLastReadMessageId(chatRoomId, memberId);
        updateUnreadMessages(chatRoom, lastReadMessageId);

        // 텍스트와 이미지 메시지 조회
        List<ChatMessage> textMessages = chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);
        List<ChatImageMessage> imageMessages = chatImageMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);

        // 통합 및 시간순 정렬
        return Stream.concat(
                        textMessages.stream().map(ChatCombinedMessageResponse::from),
                        imageMessages.stream().map(ChatCombinedMessageResponse::from)
                )
                .sorted(Comparator.comparing(ChatCombinedMessageResponse::createdAt).reversed())
                .toList();
    }

    // 채팅방 ID로 채팅방을 조회, 없으면 예외 발생
    private ChatRoom findChatRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    // 사용자 ID로 사용자를 조회, 없으면 예외 발생
    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHAT_MEMBER_NOT_FOUND));
    }

    // 사용자가 채팅방에 속해 있는지 검증, 속해 있지 않으면 예외 발생
    private void validateChatRoomMembership(ChatRoom chatRoom, Member member) {
        if (!chatRoomMemberRepository.existsByChatRoomAndMember(chatRoom, member)) {
            throw new AccessDeniedException(ErrorCode.CHAT_ACCESS_DENIED);
        }
    }

    // 채팅 메시지를 생성하고 데이터베이스에 저장
    private ChatMessage saveChatMessage(SendChatMessageRequest request, ChatRoom chatRoom, Member sender) {
        return chatMessageRepository.save(request.toEntity(chatRoom, sender));
    }

    // Redis로 메시지를 발행하고 Slack으로 알림을 전송
    private void publishMessage(Long chatRoomId, ChatMessageResponse response, Member sender, String message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(response);
            redisPublisher.publish(chatRoomId, jsonMessage);
            log.info("📤 Redis에 메시지 발행 완료: {}", response);

            String slackMessage = String.format("📢 [채팅방 %d] %s: %s", chatRoomId, sender.getNickname(), message);
            slackAlarmService.sendSlackMessage(slackMessage);
        } catch (Exception e) {
            log.error("❌ Redis 메시지 발행 중 오류 발생", e);
        }
    }

    // Redis로 메시지를 발행하고 Slack으로 알림을 전송
    private void publishImage(Long chatRoomId, ChatImageMessageResponse response, Member sender, String imgUrl) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(response);
            redisPublisher.publish(chatRoomId, jsonMessage);
            log.info("📤 Redis에 메시지 발행 완료: {}", response);

            String slackMessage = String.format("📢 [채팅방 %d] %s: %s", chatRoomId, sender.getNickname(), imgUrl);
            slackAlarmService.sendSlackMessage(slackMessage);
        } catch (Exception e) {
            log.error("❌ Redis 메시지 발행 중 오류 발생", e);
        }
    }

    // Redis에서 사용자의 마지막 읽은 메시지 ID를 조회
    private Long getLastReadMessageId(Long chatRoomId, Long memberId) {
        String key = String.format(LAST_READ_MESSAGE_KEY_TEMPLATE, chatRoomId, memberId);
        String lastReadMessageIdStr = redisTemplate.opsForValue().get(key);
        return lastReadMessageIdStr != null ? Long.parseLong(lastReadMessageIdStr) : 0L;
    }

    // 읽지 않은 메시지의 unreadCount를 감소시키고 Redis에 최신 읽은 메시지 ID를 업데이트
    private void updateUnreadMessages(ChatRoom chatRoom, Long lastReadMessageId) {
        List<ChatMessage> unreadMessages = chatMessageRepository.findByChatRoomAndIdGreaterThan(chatRoom, lastReadMessageId);
        if (!unreadMessages.isEmpty()) {
            unreadMessages.forEach(ChatMessage::decrementUnreadCount);
            chatMessageRepository.saveAll(unreadMessages);

            Long latestMessageId = unreadMessages.get(unreadMessages.size() - 1).getId();
            String key = String.format(LAST_READ_MESSAGE_KEY_TEMPLATE, chatRoom.getId(), unreadMessages.get(0).getSender().getId());
            redisTemplate.opsForValue().set(key, String.valueOf(latestMessageId));
        }
    }

    // 채팅방의 모든 메시지를 시간순으로 조회하여 응답 DTO 리스트로 변환
    private List<ChatMessageResponse> fetchAllMessages(ChatRoom chatRoom) {
        return chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom)
                .stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

}