package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.contansts.MessageConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 채팅방별 읽지 않은 메시지 수를 표현하는 Value Object
 */
public class UnreadMessageCounts {
    
    private final List<UnreadCountItem> items;
    
    private UnreadMessageCounts(List<UnreadCountItem> items) {
        this.items = Collections.unmodifiableList(
            items != null ? new ArrayList<>(items) : new ArrayList<>()
        );
    }
    
    /**
     * 채팅방 ID와 읽지 않은 메시지 수를 표현하는 내부 클래스
     */
    public static class UnreadCountItem {
        private final Long chatRoomId;
        private final Integer count;
        
        private UnreadCountItem(Long chatRoomId, Integer count) {
            this.chatRoomId = Objects.requireNonNull(chatRoomId, MessageConstants.CHAT_ROOM_ID_NOT_NULL);
            this.count = count != null ? count : 0;
        }
        
        public Long getChatRoomId() {
            return chatRoomId;
        }
        
        public Integer getCount() {
            return count;
        }
    }
    
    /**
     * 빈 UnreadMessageCounts 객체를 생성합니다.
     */
    public static UnreadMessageCounts empty() {
        return new UnreadMessageCounts(List.of());
    }
    
    /**
     * 채팅방 ID와 읽지 않은 메시지 수로 UnreadMessageCounts 객체를 생성합니다.
     */
    public static UnreadMessageCounts of(Long chatRoomId, Integer unreadCount) {
        if (chatRoomId == null) {
            return empty();
        }
        
        return new UnreadMessageCounts(List.of(new UnreadCountItem(chatRoomId, unreadCount)));
    }
    
    /**
     * 여러 UnreadCountItem으로 UnreadMessageCounts 객체를 생성합니다.
     */
    public static UnreadMessageCounts of(List<UnreadCountItem> items) {
        return new UnreadMessageCounts(items);
    }

    
    /**
     * 채팅방 메타 정보 목록에서 사용자별 안 읽은 메시지 수를 추출합니다.
     */
    public static UnreadMessageCounts fromMetaInfos(List<ChatRoomMetaInfo> metaInfos, Long userId, List<Long> chatRoomIds) {
        if (metaInfos == null || metaInfos.isEmpty() || userId == null || chatRoomIds == null) {
            return empty();
        }
        
        List<UnreadCountItem> items = metaInfos.stream()
            .filter(metaInfo -> metaInfo.getChatRoomId() != null && chatRoomIds.contains(metaInfo.getChatRoomId()))
            .map(metaInfo -> new UnreadCountItem(metaInfo.getChatRoomId(), metaInfo.getUnreadCount(userId)))
            .collect(Collectors.toList());
        
        return new UnreadMessageCounts(items);
    }
    
    /**
     * 채팅방 ID로 안 읽은 메시지 수를 조회합니다.
     */
    public Integer getUnreadCount(Long chatRoomId) {
        if (chatRoomId == null) {
            return 0;
        }
        
        return items.stream()
            .filter(item -> chatRoomId.equals(item.getChatRoomId()))
            .map(UnreadCountItem::getCount)
            .findFirst()
            .orElse(0);
    }
    
    /**
     * 모든 안 읽은 메시지 정보를 반환합니다.
     */
    public List<UnreadCountItem> getItems() {
        return items;
    }
} 