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
    

    public static UnreadMessageCounts empty() {
        return new UnreadMessageCounts(List.of());
    }

    public static UnreadMessageCounts of(Long chatRoomId, Integer unreadCount) {
        if (chatRoomId == null) {
            return empty();
        }
        
        return new UnreadMessageCounts(List.of(new UnreadCountItem(chatRoomId, unreadCount)));
    }


    public static UnreadMessageCounts of(List<UnreadCountItem> items) {
        return new UnreadMessageCounts(items);
    }


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

    public List<UnreadCountItem> getItems() {
        return items;
    }
} 