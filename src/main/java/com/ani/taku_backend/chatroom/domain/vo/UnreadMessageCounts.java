package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UnreadMessageCounts {
    
    private final Map<Long, Integer> unreadCountByChatRoomId;

    private UnreadMessageCounts(Map<Long, Integer> unreadCountMap) {
        this.unreadCountByChatRoomId = Collections.unmodifiableMap(
            unreadCountMap != null ? new HashMap<>(unreadCountMap) : new HashMap<>()
        );
    }

    public static UnreadMessageCounts empty() {
        return new UnreadMessageCounts(new HashMap<>());
    }

    public static UnreadMessageCounts fromMetaInfos(
            Collection<ChatRoomMetaInfo> metaInfos, Long userId) {
        
        if (metaInfos == null || metaInfos.isEmpty() || userId == null) {
            return empty();
        }
        
        Map<Long, Integer> unreadCountMap = new HashMap<>();
        
        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            int unreadCount = metaInfo.getUnreadCount(userId);
            unreadCountMap.put(metaInfo.getChatRoomId(), unreadCount);
        }
        
        return new UnreadMessageCounts(unreadCountMap);
    }

    
    /**
     * 채팅방 ID와 읽지 않은 메시지 수 맵으로 객체를 생성합니다.
     */
    public static UnreadMessageCounts of(Map<Long, Integer> unreadCountMap) {
        return new UnreadMessageCounts(unreadCountMap);
    }
    
    /**
     * 채팅방 ID와 읽지 않은 메시지 수 쌍으로 객체를 생성합니다.
     */
    public static UnreadMessageCounts of(Long chatRoomId, Integer unreadCount) {
        Map<Long, Integer> map = new HashMap<>();
        if (chatRoomId != null) {
            map.put(chatRoomId, unreadCount != null ? unreadCount : 0);
        }
        return new UnreadMessageCounts(map);
    }
    
    /**
     * 채팅방 ID로 읽지 않은 메시지 수를 조회합니다.
     * 값이 없는 경우 0을 반환합니다.
     */
    public int getUnreadCount(Long chatRoomId) {
        return unreadCountByChatRoomId.getOrDefault(chatRoomId, 0);
    }
    
    /**
     * 저장된 채팅방 수를 반환합니다.
     */
    public int size() {
        return unreadCountByChatRoomId.size();
    }
} 