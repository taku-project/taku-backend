package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    public static UnreadMessageCounts of(Map<Long, Integer> unreadCountMap) {
        return new UnreadMessageCounts(unreadCountMap);
    }

    public static UnreadMessageCounts of(Long chatRoomId, Integer unreadCount) {
        Map<Long, Integer> map = new HashMap<>();
        if (chatRoomId != null && unreadCount != null) {
            map.put(chatRoomId, unreadCount);
        }
        return new UnreadMessageCounts(map);
    }

    /**
     * 채팅방 메타 정보 목록에서 사용자별 안 읽은 메시지 수를 추출합니다.
     * 
     * @param metaInfos 채팅방 메타 정보 목록
     * @param userId 사용자 ID
     * @param chatRoomIds 조회할 채팅방 ID 목록 (필터링용)
     * @return 채팅방별 안 읽은 메시지 수 모음
     */
    public static UnreadMessageCounts fromMetaInfos(List<ChatRoomMetaInfo> metaInfos, Long userId, List<Long> chatRoomIds) {
        Map<Long, Integer> unreadCountMap = new HashMap<>();
        
        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            Long chatRoomId = metaInfo.getChatRoomId();
            if (chatRoomId != null && chatRoomIds.contains(chatRoomId)) {
                unreadCountMap.put(chatRoomId, metaInfo.getUnreadCount(userId));
            }
        }
        
        return UnreadMessageCounts.of(unreadCountMap);
    }

    /**
     * 채팅방 ID로 안 읽은 메시지 수를 조회합니다.
     */
    public Integer getUnreadCount(Long chatRoomId) {
        return unreadCountByChatRoomId.getOrDefault(chatRoomId, 0);
    }

} 