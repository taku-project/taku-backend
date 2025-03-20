package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import java.util.Collections;
import java.util.HashMap;
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
     * 채팅방 ID로 마지막 메시지를 조회합니다.
     */
    public Optional<ChatMessage> getLastMessage(Long chatRoomId) {
        return Optional.ofNullable(lastMessageByChatRoomId.get(chatRoomId));
    }

} 