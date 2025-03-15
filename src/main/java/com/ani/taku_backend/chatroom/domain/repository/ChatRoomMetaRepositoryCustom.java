package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import java.util.List;
import java.util.Map;

public interface ChatRoomMetaRepositoryCustom {
    
    /**
     * 여러 채팅방의 메타 정보와 마지막 메시지를 한 번에 조회합니다.
     */
    List<ChatRoomMetaInfo> findMetaInfoWithLastMessages(List<Long> chatRoomIds);
    
    /**
     * 여러 채팅방의 안 읽은 메시지 수를 한 번에 조회합니다.
     */
    Map<Long, Integer> getUnreadCountMap(List<Long> chatRoomIds, Long userId);
    
    /**
     * 여러 채팅방의 마지막 메시지를 한 번에 조회합니다.
     */
    Map<Long, ChatMessage> getLastMessageMap(List<Long> chatRoomIds);
} 