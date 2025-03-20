package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;

import java.util.Collection;
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

    public static ChatRoomMessages fromMetaInfos(Collection<ChatRoomMetaInfo> metaInfos) {
        if (metaInfos == null || metaInfos.isEmpty()) {
            return empty();
        }
        
        Map<Long, ChatMessage> lastMessageMap = new HashMap<>();
        
        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            ChatMessage lastMessage = metaInfo.getLastMessage();
            if (lastMessage != null) {
                lastMessageMap.put(metaInfo.getChatRoomId(), lastMessage);
            }
        }
        
        return new ChatRoomMessages(lastMessageMap);
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

    public Optional<ChatMessage> getLastMessage(Long chatRoomId) {
        return Optional.ofNullable(lastMessageByChatRoomId.get(chatRoomId));
    }

    public int size() {
        return lastMessageByChatRoomId.size();
    }
} 