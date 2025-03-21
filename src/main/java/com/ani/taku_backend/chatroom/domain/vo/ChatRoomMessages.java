package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class ChatRoomMessages {
    
    private final Map<Long, ChatMessage> lastMessageByChatRoomId;

    private ChatRoomMessages(Map<Long, ChatMessage> lastMessageMap) {
        this.lastMessageByChatRoomId = Collections.unmodifiableMap(
            lastMessageMap != null ? new HashMap<>(lastMessageMap) : new HashMap<>()
        );
    }

    public static ChatRoomMessages empty() {
        return new ChatRoomMessages(new HashMap<>());
    }


    public static ChatRoomMessages of(Map<Long, ChatMessage> lastMessageMap) {
        return new ChatRoomMessages(lastMessageMap);
    }

    public static ChatRoomMessages of(Long chatRoomId, ChatMessage lastMessage) {
        Map<Long, ChatMessage> map = new HashMap<>();
        if (chatRoomId != null && lastMessage != null) {
            map.put(chatRoomId, lastMessage);
        }
        return new ChatRoomMessages(map);
    }
    
    /**
     * 채팅방 메타 정보 목록에서 마지막 메시지를 추출하여 생성합니다.
     * 
     * @param metaInfos 채팅방 메타 정보 목록
     * @return 채팅방별 마지막 메시지 모음
     */
    public static ChatRoomMessages fromMetaInfos(List<ChatRoomMetaInfo> metaInfos) {
        Map<Long, ChatMessage> lastMessageMap = new HashMap<>();
        
        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            Long chatRoomId = metaInfo.getChatRoomId();
            ChatMessage lastMessage = metaInfo.getLastMessage();
            if (chatRoomId != null && lastMessage != null) {
                lastMessageMap.put(chatRoomId, lastMessage);
            }
        }
        
        return ChatRoomMessages.of(lastMessageMap);
    }

    /**
     * 채팅방 ID로 마지막 메시지를 조회합니다.
     */
    public Optional<ChatMessage> getLastMessage(Long chatRoomId) {
        return Optional.ofNullable(lastMessageByChatRoomId.get(chatRoomId));
    }

} 