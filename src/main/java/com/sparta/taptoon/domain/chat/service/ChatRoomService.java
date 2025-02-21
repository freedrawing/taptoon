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
import com.sparta.taptoon.global.redis.RedisSubscriptionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final RedisSubscriptionManager redisSubscriptionManager;
    private final ChatMessageRepository chatMessageRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public ChatRoomResponse createChatRoom(Long ownerId, CreateChatRoomRequest request) {

        Member creator = memberRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        // 채팅방 생성
        ChatRoom chatRoom = chatRoomRepository.save(CreateChatRoomRequest.toEntity());

        // 요청된 멤버들을 찾아서 채팅방에 추가
        List<Member> members = memberRepository.findAllById(request.memberIds());
        if (members.size() < 2) {
            throw new IllegalArgumentException("채팅방은 최소 2명 이상이어야 합니다.");
        }

        // 채팅방에 멤버 추가
        chatRoom.addMember(creator); // 생성자 추가
        members.forEach(chatRoom::addMember);

        log.info("✅ 단체 채팅방 생성 완료 (참여 인원: {}명)", members.size());

        // Redis 구독 추가
        redisSubscriptionManager.subscribeChatRoom(chatRoom.getId());

        return ChatRoomResponse.from(chatRoom);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> getChatRooms(Long memberId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByMembers_MemberIdAndIsDeletedFalse(memberId);

        return chatRooms.stream()
                .map(chatRoom -> {
                    // 채팅방의 마지막 메시지 조회
                    ChatMessage lastMessage = chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(chatRoom)
                            .orElse(null);

                    String lastMessageContent = (lastMessage != null) ? lastMessage.getMessage() : "대화가 없습니다.";
                    String lastMessageTime = (lastMessage != null) ? lastMessage.getCreatedAt().toString() : null;

                    // 사용자의 마지막 읽은 메시지 ID 조회 (Redis)
                    String lastReadMessageKey = "chat:room:" + chatRoom.getId() + ":user:" + memberId;
                    String lastReadMessageIdStr = redisTemplate.opsForValue().get(lastReadMessageKey);
                    Long lastReadMessageId = (lastReadMessageIdStr != null) ? Long.parseLong(lastReadMessageIdStr) : 0L;

                    // 안 읽은 메시지 개수 조회
                    int unreadCount = chatMessageRepository.countByChatRoomAndIdGreaterThan(chatRoom, lastReadMessageId);

                    return ChatRoomListResponse.of(chatRoom, lastMessageContent, lastMessageTime, unreadCount);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteChatRoom(Long memberId, Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        chatRoom.delete(); // 소프트 삭제 처리
        log.info("✅ 채팅방 {} 삭제 완료", chatRoomId);
    }
}
