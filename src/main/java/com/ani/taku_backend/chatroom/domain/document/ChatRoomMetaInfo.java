package com.ani.taku_backend.chatroom.domain.document;

import com.ani.taku_backend.chatroom.domain.constant.MarketRole;
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
    private Instant updateAt;

    private boolean isActive = true;
    

    private List<ChatMessage> messages = new ArrayList<>();
    private int messageCount = 0;

    public ChatRoomMetaInfo() {
    }

    /**
     * 지정된 채팅방 ID로 메타 정보를 초기화합니다.
     *
     * @param chatRoomId 채팅방의 고유 ID
     */
    @Builder
    public ChatRoomMetaInfo(@Param("chatRoomId") Long chatRoomId) {
        this.chatRoomId = chatRoomId;
        this.participants = new Participants();
        this.updateAt = Instant.now();
    }

    public void initializeParticipants(Long buyerId, Long sellerId) {
        this.participants.addParticipant(buyerId, MarketRole.BUYER);
        this.participants.addParticipant(sellerId, MarketRole.SELLER);
    }

    /*
     * 모든 참가자가 방을 나갔다면 방 상태를 비활성화하는 함수
     * 이때 CharRoom 도 같이 deactivate 해준다.
     */
    public void checkAndDeactivate() {
        if (participants.allParticipantsInactive()) {
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
        this.updateAt = Instant.now();
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
        this.updateAt = Instant.now();
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

}