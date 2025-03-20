package com.ani.taku_backend.chatroom.domain.document;

import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 채팅방의 메타 정보를 관리하는 클래스입니다.
 * 참가자 정보, 메시지 목록, 마지막 메세지 ID, 업데이트 시간 및 활성 상태 등을 포함합니다.
 */

@Document(collection = "chat_room_meta")
@Getter
@Setter
public class ChatRoomMetaInfo {
    @Id
    private String id;
    @Field("chatRoomId")
    private Long chatRoomId;
    private Participants participants;

    private String lastMessageId;
    private Instant lastMessageAt;

    private boolean isActive = true;
    

    private List<ChatMessage> messages = new ArrayList<>();
    private int messageCount = 0;

    public ChatRoomMetaInfo() {
    }

    @Builder
    public ChatRoomMetaInfo(
            @Param("chatRoomId") Long chatRoomId,
            @Param("participants") Participants participants) {
        this.chatRoomId = chatRoomId;
        this.participants = participants != null ? participants : new Participants();
        this.lastMessageAt = Instant.now();
    }

    public static ChatRoomMetaInfo createWithParticipants(
            Long chatRoomId, Long buyerId, Long sellerId) {
        Participants participants = new Participants();
        participants.addParticipant(buyerId, JangterChatRole.BUYER);
        participants.addParticipant(sellerId, JangterChatRole.SELLER);

        return ChatRoomMetaInfo.builder()
                .chatRoomId(chatRoomId)
                .participants(participants)
                .build();
    }

    /*
     * 모든 참가자가 방을 나갔다면 방 상태를 비활성화하는 함수
     * 이때 CharRoom 도 같이 deactivate 해준다.
     */
    public void checkAndDeactivate() {
        if (participants.areAllParticipantsInactive()) {
            this.isActive = false;
        }
    }

    /**
     * 특정 사용자의 안 읽은 메시지 개수를 초기화합니다.
     */
    public boolean resetUnreadCount(Long userId) {
        ParticipantInfo participantInfo = this.participants.getInfo().get(userId);
        if (participantInfo != null) {
            participantInfo.resetMessageStock();
            return true;
        }
        return false;
    }

    /**
     * 새 메시지가 도착했을 때 다른 참여자들의 안 읽은 메시지 카운터를 증가시킵니다.
     */
    public void handleNewMessage(String messageId, Long senderId) {
        this.lastMessageId = messageId;
        this.participants.getInfo().forEach((userId, participantInfo) -> {
            if (!userId.equals(senderId)) {
                participantInfo.plusMessage();
            }
        });
        this.lastMessageAt = Instant.now();
    }

    /**
     * 사용자의 안 읽은 메시지 개수를 조회합니다.
     */
    public int getUnreadCount(Long userId) {
        ParticipantInfo participantInfo = this.participants.getInfo().get(userId);
        return participantInfo != null ? participantInfo.getMessageStock() : 0;
    }

    /**
     * 여러 채팅방에 대한 특정 사용자의 총 안 읽은 메시지 개수를 계산합니다.
     */
    public static int calculateTotalUnreadCount(List<ChatRoomMetaInfo> chatRooms, Long userId) {
        return chatRooms.stream()
                .map(chatroom -> chatroom.getUnreadCount(userId))
                .reduce(0, Integer::sum);
    }
    
    /**
     * 새 메시지를 채팅방에 추가합니다.
     *
     * @param message 추가할 메시지
     */
    public void addMessage(ChatMessage message) {
        messages.add(message);
        messageCount++;
        lastMessageId = message.getId();
        handleNewMessage(message.getId(), message.getSenderId());
        this.lastMessageAt = Instant.now();
    }
    
    /**
     * 특정 ID의 메시지를 조회합니다.
     *
     * @param messageId 메시지 ID
     * @return 메시지 (Optional)
     */
    public Optional<ChatMessage> findMessageById(String messageId) {
        return messages.stream()
                .filter(msg -> msg.getId().equals(messageId))
                .findFirst();
    }
    
    /**
     * 채팅방의 메시지를 최신순으로 limit 개수만큼 반환합니다.
     *
     * @param limit 조회할 메시지 수
     * @return 최신 메시지 목록
     */
    public List<ChatMessage> getRecentMessages(int limit) {
        int size = messages.size();
        if (size <= limit) {
            return new ArrayList<>(messages);
        }
        return new ArrayList<>(messages.subList(size - limit, size));
    }
    
    /**
     * 특정 시간 이전의 메시지를 조회합니다. (무한 스크롤용)
     *
     * @param before 기준 시간
     * @param limit 조회할 메시지 수
     * @return 조건에 맞는 메시지 목록
     */
    public List<ChatMessage> getMessagesBeforeTime(LocalDateTime before, int limit) {
        List<ChatMessage> result = new ArrayList<>();
        int count = 0;
        
        // 최신 메시지부터 역순으로 조회
        for (int i = messages.size() - 1; i >= 0 && count < limit; i--) {
            ChatMessage message = messages.get(i);
            if (message.getSentAt().isBefore(before)) {
                result.add(message);
                count++;
            }
        }
        
        return result;
    }
    
    /**
     * 채팅방의 모든 메시지를 읽음 처리합니다.
     *
     * @param userId 읽는 사용자 ID
     */
    public void markMessagesAsRead(Long userId) {
        messages.forEach(message -> {
            if (!message.getSenderId().equals(userId) && !message.getRead()) {
                message.markAsRead();
            }
        });
    }

    /**
     * 사용자가 채팅방을 나갈 때 처리하는 메서드입니다.
     * 참여자 검증, 상태 변경, 비활성화 처리를 캡슐화합니다.
     *
     * @param userId 나가려는 사용자 ID
     * @return 모든 참여자가 나갔는지 여부
     * @throws DuckwhoException 유효하지 않은 사용자인 경우 발생
     */
    public boolean leaveRoom(Long userId) {
        if (!this.participants.containsUser(userId)) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }

        this.participants.deactivateUser(userId);
        this.checkAndDeactivate();

        return this.participants.isAllInactive();
    }

    /**
     * 사용자의 채팅방 접근 권한을 검증합니다.
     *
     * @param userId 검증할 사용자 ID
     * @throws DuckwhoException 유효하지 않은 사용자인 경우 발생
     */
    public void validateUserAccess(Long userId) {
        if (!this.participants.containsUser(userId)) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }
    }

    /**
     * 사용자의 모든 메시지를 읽음 처리합니다.
     * 참여자 검증 및 읽음 처리, 카운터 초기화를 캡슐화합니다.
     *
     * @param userId 읽음 처리할 사용자 ID
     * @throws DuckwhoException 유효하지 않은 사용자인 경우 발생
     */
    public void readAllMessages(Long userId) {
        validateUserAccess(userId);
        markMessagesAsRead(userId);
        resetUnreadCount(userId);
    }

    /**
     * 현재 채팅방의 마지막 메시지를 가져옵니다.
     * @return 마지막 메시지 또는 비어있을 경우 null
     */
    public ChatMessage getLastMessage() {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        return messages.get(messages.size() - 1);
    }
}