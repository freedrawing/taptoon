package com.sparta.taptoon.domain.chat.service;

import com.sparta.taptoon.domain.chat.dto.request.CreateChatRoomRequest;
import com.sparta.taptoon.domain.chat.dto.response.ChatRoomListResponse;
import com.sparta.taptoon.domain.chat.dto.response.ChatRoomResponse;
import com.sparta.taptoon.domain.chat.entity.ChatMessage;
import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.chat.event.ChatRoomCreatedEvent;
import com.sparta.taptoon.domain.chat.repository.ChatMessageRepository;
import com.sparta.taptoon.domain.chat.repository.ChatRoomRepository;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.InvalidRequestException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private static final String LAST_READ_MESSAGE_KEY_TEMPLATE = "chat:room:%s:user:%d"; // String으로 변경

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final StringRedisTemplate redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 새로운 채팅방을 생성하고 멤버를 추가, Redis 채널 구독을 이벤트로 처리.
     *
     * @param ownerId 채팅방을 생성하는 사용자의 ID
     * @param request 초대할 멤버 ID 목록이 담긴 요청
     * @return 생성된 채팅방의 응답 DTO
     */
    @Transactional
    public ChatRoomResponse createChatRoom(Long ownerId, CreateChatRoomRequest request) {
        Member creator = findMember(ownerId);
        validateSelfInvitation(ownerId, request);
        List<Long> inviteeIds = removeDuplicateIds(request.memberIds());
        validateInvitees(inviteeIds);

        List<Long> allMemberIds = new ArrayList<>(inviteeIds);
        allMemberIds.add(ownerId);
        Collections.sort(allMemberIds);

        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findByExactMembers(allMemberIds, allMemberIds.size());
        if (existingChatRoom.isPresent()) {
            log.info("✅ 기존 채팅방 반환 - chatRoomId: {}, memberCount: {}",
                    existingChatRoom.get().getId(), existingChatRoom.get().getMemberIds().size());
            return ChatRoomResponse.from(existingChatRoom.get());
        }

        List<Member> members = fetchMembers(inviteeIds);
        ChatRoom chatRoom = createAndSaveChatRoom(creator, members, allMemberIds);

        eventPublisher.publishEvent(new ChatRoomCreatedEvent(chatRoom.getId())); // Redis 구독을 이벤트로 처리
        log.info("✅ 단체 채팅방 생성 완료 (참여 인원: {}명)", chatRoom.getMemberIds().size());
        return ChatRoomResponse.from(chatRoom);
    }

    /**
     * 사용자가 속한 채팅방 목록을 조회
     * 각 채팅방의 마지막 메시지와 읽지 않은 메시지 수를 포함.
     *
     * @param memberId 채팅방 목록을 조회하는 사용자의 ID
     * @return 채팅방 목록 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> getChatRooms(Long memberId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByMemberIdsContainsAndIsDeletedFalse(memberId);
        return chatRooms.stream()
                .map(chatRoom -> buildChatRoomListResponse(chatRoom, memberId))
                .toList();
    }

    /**
     * 채팅방을 삭제 상태로 변경 (소프트 삭제).
     *
     * @param memberId 채팅방을 삭제하려는 사용자의 ID
     * @param chatRoomId 삭제할 채팅방 ID
     */
    @Transactional
    public void deleteChatRoom(Long memberId, String chatRoomId) {
        ChatRoom chatRoom = findChatRoom(chatRoomId);
        chatRoom.delete();
        chatRoomRepository.save(chatRoom); // MongoDB에서는 명시적 저장 필요
        log.info("✅ 채팅방 {} 삭제 완료", chatRoomId);
    }

    // 주어진 사용자 ID로 사용자를 조회, 없으면 예외 발생
    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHAT_MEMBER_NOT_FOUND));
    }

    // 주어진 채팅방 ID로 채팅방을 조회, 없으면 예외 발생
    public ChatRoom findChatRoom(String chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    // 사용자가 자기 자신을 초대하는지 검증, 시도 시 예외 발생
    private void validateSelfInvitation(Long ownerId, CreateChatRoomRequest request) {
        if (request.memberIds().contains(ownerId)) {
            throw new InvalidRequestException(ErrorCode.CHAT_SELF_INVITATION);
        }
    }

    // 초대 멤버 ID 목록에서 중복을 제거
    private List<Long> removeDuplicateIds(List<Long> memberIds) {
        return memberIds.stream().distinct().toList();
    }

    // 초대 멤버 목록이 유효한지 검증, 비어 있으면 예외 발생
    private void validateInvitees(List<Long> inviteeIds) {
        if (inviteeIds.isEmpty()) {
            throw new InvalidRequestException(ErrorCode.CHAT_NO_VALID_INVITEES);
        }
    }

    // 초대된 멤버들을 데이터베이스에서 조회, 한 명도 초대 안 할 시 예외 발생
    private List<Member> fetchMembers(List<Long> memberIds) {
        List<Member> members = memberRepository.findAllById(memberIds);
        if (members.size() < 1) {
            throw new InvalidRequestException(ErrorCode.CHAT_MINIMUM_MEMBERS);
        }
        return members;
    }

    // 채팅방을 생성하고 만든 사람과 초대된 멤버들을 추가
    private ChatRoom createAndSaveChatRoom(Member creator, List<Member> members, List<Long> allMemberIds) {
        ChatRoom chatRoom = ChatRoom.builder()
                .memberIds(allMemberIds)
                .build();
        return chatRoomRepository.save(chatRoom);
    }

    // 채팅방의 마지막 메시지와 읽지 않은 메시지 수를 포함한 응답 DTO를 생성
    private ChatRoomListResponse buildChatRoomListResponse(ChatRoom chatRoom, Long memberId) {
        ChatMessage lastMessage = chatMessageRepository.findTopByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId())
                .orElse(null);
        String lastMessageContent = lastMessage != null ? lastMessage.getMessage() : "대화가 없습니다.";
        String lastMessageTime = lastMessage != null ? lastMessage.getCreatedAt().toString() : null;
        int unreadCount = calculateUnreadCount(chatRoom, memberId);

        return ChatRoomListResponse.from(chatRoom, lastMessageContent, lastMessageTime, unreadCount);
    }

    // Redis와 데이터베이스를 통해 읽지 않은 메시지 수를 계산
    public int calculateUnreadCount(ChatRoom chatRoom, Long memberId) {
        String key = String.format(LAST_READ_MESSAGE_KEY_TEMPLATE, chatRoom.getId(), memberId);
        String lastReadMessageIdStr = redisTemplate.opsForValue().get(key);
        ObjectId lastReadMessageId = lastReadMessageIdStr != null ? new ObjectId(lastReadMessageIdStr) : null;

        int unreadCount;
        if (lastReadMessageId == null) {
            unreadCount = chatMessageRepository.countByChatRoomId(chatRoom.getId());
            log.info("No last read message for memberId: {}, counting all messages: {}", memberId, unreadCount);
        } else {
            // senderId 조건 제거하고 전체 unread 계산
            unreadCount = chatMessageRepository.countUnreadMessagesExcludingSender(
                    chatRoom.getId(), lastReadMessageId, memberId
            );
            log.info("Counting unread - chatRoomId: {}, memberId: {}, lastReadMessageId: {}, unreadCount: {}",
                    chatRoom.getId(), memberId, lastReadMessageId, unreadCount);
        }
        return unreadCount;
    }

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
            log.info("✅ 읽음 처리 완료 - chatRoomId: {}, memberId: {}, latestMessageId: {}",
                    chatRoom.getId(), memberId, latestMessageId);
        } else {
            log.info("No unread messages to update - chatRoomId: {}, memberId: {}, lastReadMessageId: {}",
                    chatRoom.getId(), memberId, lastReadMessageId);
        }
    }

    // 채팅방 멤버인지 검증
    public boolean isMemberOfChatRoom(String chatRoomId, Long memberId) {
        return chatRoomRepository.existsByIdAndMemberId(chatRoomId, memberId);
    }
}