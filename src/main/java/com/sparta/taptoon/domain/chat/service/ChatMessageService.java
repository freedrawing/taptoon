package com.sparta.taptoon.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.taptoon.domain.chat.dto.request.SendChatMessageRequest;
import com.sparta.taptoon.domain.chat.dto.response.ChatCombinedMessageResponse;
import com.sparta.taptoon.domain.chat.dto.response.ChatMessageResponse;
import com.sparta.taptoon.domain.chat.entity.ChatImageMessage;
import com.sparta.taptoon.domain.chat.entity.ChatMessage;
import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.chat.repository.ChatImageMessageRepository;
import com.sparta.taptoon.domain.chat.repository.ChatMessageRepository;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import com.sparta.taptoon.global.redis.RedisPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private static final String LAST_READ_MESSAGE_KEY_TEMPLATE = "chat:room:%s:user:%d";

    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final RedisPublisher redisPublisher;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final ChatImageMessageRepository chatImageMessageRepository;
    private final ChatRoomService chatRoomService;


    /**
     * 메시지 전송 & 저장, Redis와 Slack으로 알림을 발행합니다.
     *
     * @param senderId 메시지를 보내는 사용자의 ID
     * @param chatRoomId 메시지가 전송될 채팅방 ID
     * @param request 메시지 내용이 담긴 요청
     * @return 저장된 메시지의 응답 DTO
     */
    public ChatMessageResponse sendMessage(Long senderId, String chatRoomId, SendChatMessageRequest request) {
        ChatRoom chatRoom = chatRoomService.findChatRoom(chatRoomId);
        Member sender = findMember(senderId);
        validateChatRoomMembership(chatRoom, sender);

        ChatMessage chatMessage = saveChatMessage(request, chatRoom, sender);
        ChatMessageResponse response = ChatMessageResponse.from(chatMessage);

        updateLastReadMessage(chatRoomId, senderId, chatMessage.getId());
        publishMessage(chatRoom.getId(), response, sender, request.message());
        return response;
    }

//    @Transactional
//    public ChatImageMessageResponse sendImageMessage(Long senderId, String chatRoomId, SendChatImageMessageRequest request) {
//        ChatRoom chatRoom = findChatRoom(chatRoomId);
//        Member sender = findMember(senderId);
//        validateChatRoomMembership(chatRoom, sender);
//
//        ChatImageMessage imageMessage = chatImageMessageRepository.findByIdAndChatRoom(request.imageMessageId(), chatRoom)
//                .orElseThrow(() -> new IllegalArgumentException("이미지 메시지를 찾을 수 없습니다."));
//
//        if (!imageMessage.getSender().getId().equals(senderId)) {
//            throw new AccessDeniedException("본인이 업로드한 이미지만 전송할 수 있습니다.");
//        }
//        if (imageMessage.getStatus() != Status.PENDING) {
//            throw new AccessDeniedException("이미 전송된 이미지입니다.");
//        }
//
//        imageMessage.updateStatus(Status.REGISTERED);
//        imageMessage.setUnreadCount(chatRoom.getMemberCount() - 1); // 전송 시 읽지 않은 멤버 수 설정
//        chatImageMessageRepository.save(imageMessage);
//
////        // 이미지 메시지도 읽음 처리
////        String key = String.format(LAST_READ_MESSAGE_KEY_TEMPLATE, chatRoomId, senderId);
////        redisTemplate.opsForValue().set(key, String.valueOf(imageMessage.getId()));
////        log.info("✅ 이미지 메시지 전송 및 읽음 처리 완료 - chatRoomId: {}, senderId: {}, imageMessageId: {}",
////                chatRoomId, senderId, imageMessage.getId());
//
//        ChatImageMessageResponse response = ChatImageMessageResponse.from(imageMessage);
//        publishImage(chatRoom.getId(), response, sender, "이미지 메시지 전송");
//        return response;
//    }

    /**
     * 채팅방의 모든 메시지를 조회하고, 읽지 않은 메시지를 읽음 처리.
     *
     * @param memberId 메시지를 조회하는 사용자의 ID
     * @param chatRoomId 조회할 채팅방 ID
     * @return 채팅방의 메시지 목록
     */
    @Transactional
    public List<ChatCombinedMessageResponse> getChatMessages(Long memberId, String chatRoomId) {
        ChatRoom chatRoom = chatRoomService.findChatRoom(chatRoomId);
        Member member = findMember(memberId);
        validateChatRoomMembership(chatRoom, member);

        updateUnreadMessages(chatRoom, memberId); // 채팅방 진입 시 읽음 처리

        List<ChatMessage> textMessages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId);
        List<ChatImageMessage> imageMessages = chatImageMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId);

        return Stream.concat(
                        textMessages.stream().map(ChatCombinedMessageResponse::from),
                        imageMessages.stream().map(ChatCombinedMessageResponse::from)
                )
                .sorted(Comparator.comparing(ChatCombinedMessageResponse::createdAt))
                .toList();
    }

    // Redis에 마지막 읽은 메시지 저장
    public void updateLastReadMessage(String chatRoomId, Long memberId, String messageId) {
        String key = String.format(LAST_READ_MESSAGE_KEY_TEMPLATE, chatRoomId, memberId);
        redisTemplate.opsForValue().set(key, messageId);
        log.info("✅ 읽음 처리 - chatRoomId: {}, memberId: {}, messageId: {}", chatRoomId, memberId, messageId);
    }

    // 읽지 않은 메시지 수 계산
    public int calculateUnreadCount(String chatRoomId, Long memberId) {
        String key = String.format(LAST_READ_MESSAGE_KEY_TEMPLATE, chatRoomId, memberId);
        String lastReadMessageIdStr = redisTemplate.opsForValue().get(key);
        ObjectId lastReadMessageId = lastReadMessageIdStr != null ? new ObjectId(lastReadMessageIdStr) : null;

        int unreadCount;
        if (lastReadMessageId == null) {
            unreadCount = chatMessageRepository.countByChatRoomId(chatRoomId);
            log.info("No last read message for memberId: {}, counting all messages: {}", memberId, unreadCount);
        } else {
            unreadCount = chatMessageRepository.countUnreadMessagesExcludingSender(chatRoomId, lastReadMessageId, memberId);
            log.info("Counting unread - chatRoomId: {}, memberId: {}, lastReadMessageId: {}, unreadCount: {}",
                    chatRoomId, memberId, lastReadMessageId, unreadCount);
        }
        return unreadCount;
    }

    // 채팅방 조회 시 읽음 처리
    @Transactional
    public void updateUnreadMessages(ChatRoom chatRoom, Long memberId) {
        String key = String.format(LAST_READ_MESSAGE_KEY_TEMPLATE, chatRoom.getId(), memberId);
        String lastReadMessageIdStr = redisTemplate.opsForValue().get(key);
        ObjectId lastReadMessageId = lastReadMessageIdStr != null ? new ObjectId(lastReadMessageIdStr) : null;

        List<ChatMessage> unreadMessages = lastReadMessageId == null
                ? chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoom.getId())
                : chatMessageRepository.findByChatRoomIdAndIdGreaterThan(chatRoom.getId(), lastReadMessageId);

        if (!unreadMessages.isEmpty()) {
            unreadMessages.forEach(message -> {
                if (message.getUnreadCount() > 0 && !message.getSenderId().equals(memberId)) {
                    message.decrementUnreadCount();
                }
            });
            chatMessageRepository.saveAll(unreadMessages);

            String latestMessageId = unreadMessages.get(unreadMessages.size() - 1).getId();
            redisTemplate.opsForValue().set(key, latestMessageId);
            log.info("✅ 읽음 처리 완료 - chatRoomId: {}, memberId: {}, latestMessageId: {}", chatRoom.getId(), memberId, latestMessageId);
        }
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHAT_MEMBER_NOT_FOUND));
    }

    private void validateChatRoomMembership(ChatRoom chatRoom, Member member) {
        if (!chatRoom.getMemberIds().contains(member.getId())) {
            log.warn("Access denied for memberId: {} to chatRoomId: {}", member.getId(), chatRoom.getId());
            throw new AccessDeniedException(ErrorCode.CHAT_ACCESS_DENIED);
        }
        log.info("Access granted for memberId: {} to chatRoomId: {}", member.getId(), chatRoom.getId());
    }

    private ChatMessage saveChatMessage(SendChatMessageRequest request, ChatRoom chatRoom, Member sender) {
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoomId(chatRoom.getId())
                .senderId(sender.getId())
                .message(request.message())
                .unreadCount(chatRoom.getMemberIds().size() - 1)
                .build();

        return chatMessageRepository.save(chatMessage);
    }

    private void publishMessage(String chatRoomId, ChatMessageResponse response, Member sender, String message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(response);
            redisPublisher.publish(chatRoomId, jsonMessage);
            log.info("📤 Redis에 메시지 발행 완료: {}", response);
        } catch (Exception e) {
            log.error("❌ Redis 메시지 발행 중 오류 발생", e);
        }
    }

    public Optional<ChatMessage> findLatestMessage(String chatRoomId) {
        return chatMessageRepository.findTopByChatRoomIdOrderByCreatedAtDesc(chatRoomId);
    }


    // 채팅방의 모든 메시지를 시간순으로 조회하여 응답 DTO 리스트로 변환
    private List<ChatMessageResponse> fetchAllMessages(ChatRoom chatRoom) {
        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoom.getId())
                .stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

}