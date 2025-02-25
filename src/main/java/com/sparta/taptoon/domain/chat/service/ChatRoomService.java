package com.sparta.taptoon.domain.chat.service;

import com.sparta.taptoon.domain.chat.dto.request.CreateChatRoomRequest;
import com.sparta.taptoon.domain.chat.dto.response.ChatRoomListResponse;
import com.sparta.taptoon.domain.chat.dto.response.ChatRoomResponse;
import com.sparta.taptoon.domain.chat.entity.ChatMessage;
import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.chat.repository.ChatMessageRepository;
import com.sparta.taptoon.domain.chat.repository.ChatRoomRepository;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.InvalidRequestException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import com.sparta.taptoon.global.redis.RedisSubscriptionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private static final String LAST_READ_MESSAGE_KEY_TEMPLATE = "chat:room:%d:user:%d";

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final RedisSubscriptionManager redisSubscriptionManager;
    private final ChatMessageRepository chatMessageRepository;
    private final StringRedisTemplate redisTemplate;

    /**
     * 새로운 채팅방을 생성하고 멤버를 추가, Redis 채널 구독.
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

        // 전체 멤버 리스트 (ownerId 포함)
        List<Long> allMemberIds = new ArrayList<>(inviteeIds);
        allMemberIds.add(ownerId);
        Collections.sort(allMemberIds); // 순서 무관 비교를 위해 정렬

        // 동일 멤버 조합의 기존 채팅방 확인
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findByExactMembers(allMemberIds, allMemberIds.size());
        if (existingChatRoom.isPresent()) {
            log.info("✅ 기존 채팅방 반환 - chatRoomId: {}, memberCount: {}",
                    existingChatRoom.get().getId(), existingChatRoom.get().getMemberCount());
            return ChatRoomResponse.from(existingChatRoom.get());
        }

        List<Member> members = fetchMembers(inviteeIds);
        ChatRoom chatRoom = createAndSaveChatRoom(creator, members);

        redisSubscriptionManager.subscribeChatRoom(chatRoom.getId());
        log.info("✅ 단체 채팅방 생성 완료 (참여 인원: {}명)", chatRoom.getMemberCount());
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
        List<ChatRoom> chatRooms = chatRoomRepository.findByMembers_MemberIdAndIsDeletedFalse(memberId);
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
    public void deleteChatRoom(Long memberId, Long chatRoomId) {
        ChatRoom chatRoom = findChatRoom(chatRoomId);
        chatRoom.delete();
        log.info("✅ 채팅방 {} 삭제 완료", chatRoomId);
    }

    // 주어진 사용자 ID로 사용자를 조회, 없으면 예외 발생
    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHAT_MEMBER_NOT_FOUND));
    }

    // 주어진 채팅방 ID로 채팅방을 조회, 없으면 예외 발생
    private ChatRoom findChatRoom(Long chatRoomId) {
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

    // 초대된 멤버들을 데이터베이스에서 조회, 한명도 초대 안할시 예외 발생
    private List<Member> fetchMembers(List<Long> memberIds) {
        List<Member> members = memberRepository.findAllById(memberIds);
        if (members.size() < 1) {
            throw new InvalidRequestException(ErrorCode.CHAT_MINIMUM_MEMBERS);
        }
        return members;
    }

    // 채팅방을 생성하고 만든사랑과 초대된 멤버들을 추가
    private ChatRoom createAndSaveChatRoom(Member creator, List<Member> members) {
        ChatRoom chatRoom = chatRoomRepository.save(CreateChatRoomRequest.toEntity());
        chatRoom.addMember(creator);
        members.forEach(chatRoom::addMember);
        return chatRoom;
    }

    // 채팅방의 마지막 메시지와 읽지 않은 메시지 수를 포함한 응답 DTO를 생성
    private ChatRoomListResponse buildChatRoomListResponse(ChatRoom chatRoom, Long memberId) {
        ChatMessage lastMessage = chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(chatRoom)
                .orElse(null);
        String lastMessageContent = lastMessage != null ? lastMessage.getMessage() : "대화가 없습니다.";
        String lastMessageTime = lastMessage != null ? lastMessage.getCreatedAt().toString() : null;
        int unreadCount = calculateUnreadCount(chatRoom, memberId);

        return ChatRoomListResponse.of(chatRoom, lastMessageContent, lastMessageTime, unreadCount);
    }

    // Redis와 데이터베이스를 통해 읽지 않은 메시지 수를 계산
    private int calculateUnreadCount(ChatRoom chatRoom, Long memberId) {
        String key = String.format(LAST_READ_MESSAGE_KEY_TEMPLATE, chatRoom.getId(), memberId);
        String lastReadMessageIdStr = redisTemplate.opsForValue().get(key);
        Long lastReadMessageId = lastReadMessageIdStr != null ? Long.parseLong(lastReadMessageIdStr) : 0L;
        return chatMessageRepository.countByChatRoomAndIdGreaterThan(chatRoom, lastReadMessageId);
    }
}
