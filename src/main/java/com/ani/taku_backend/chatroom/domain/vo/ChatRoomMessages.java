package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.contansts.MessageConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 채팅방별 마지막 메시지 정보를 표현하는 Value Object
 */
public class ChatRoomMessages {
    
    private final List<MessageItem> items;

    private ChatRoomMessages(List<MessageItem> items) {
        this.items = Collections.unmodifiableList(
            items != null ? new ArrayList<>(items) : new ArrayList<>()
        );
    }
    
    /**
     * 채팅방 ID와 마지막 메시지를 표현하는 내부 클래스
     */
    public static class MessageItem {
        private final Long chatRoomId;
        private final ChatMessage message;
        
        private MessageItem(Long chatRoomId, ChatMessage message) {
            this.chatRoomId = Objects.requireNonNull(chatRoomId, MessageConstants.CHAT_ROOM_ID_NOT_NULL);
            this.message = message; // message는 null 허용
        }
        
        public Long getChatRoomId() {
            return chatRoomId;
        }
        
        public ChatMessage getMessage() {
            return message;
        }
    }

    /**
     * 빈 ChatRoomMessages 객체를 생성합니다.
     */
    public static ChatRoomMessages empty() {
        return new ChatRoomMessages(List.of());
    }

    /**
     * 채팅방 ID와 마지막 메시지로 ChatRoomMessages 객체를 생성합니다.
     */
    public static ChatRoomMessages of(Long chatRoomId, ChatMessage lastMessage) {
        if (chatRoomId == null) {
            return empty();
        }
        
        return new ChatRoomMessages(List.of(new MessageItem(chatRoomId, lastMessage)));
    }
    
    /**
     * 여러 MessageItem으로 ChatRoomMessages 객체를 생성합니다.
     */
    public static ChatRoomMessages of(List<MessageItem> items) {
        return new ChatRoomMessages(items);
    }

    /**
     * 채팅방 메타 정보 목록에서 마지막 메시지를 추출하여 생성합니다.
     */
    public static ChatRoomMessages fromMetaInfos(List<ChatRoomMetaInfo> metaInfos) {
        if (metaInfos == null || metaInfos.isEmpty()) {
            return empty();
        }
        
        List<MessageItem> items = metaInfos.stream()
            .filter(metaInfo -> metaInfo.getChatRoomId() != null)
            .map(metaInfo -> new MessageItem(metaInfo.getChatRoomId(), metaInfo.getLastMessage()))
            .collect(Collectors.toList());
        
        return ChatRoomMessages.of(items);
    }

    /**
     * 채팅방 ID로 마지막 메시지를 조회합니다.
     */
    public Optional<ChatMessage> getLastMessage(Long chatRoomId) {
        if (chatRoomId == null) {
            return Optional.empty();
        }
        
        return items.stream()
            .filter(item -> chatRoomId.equals(item.getChatRoomId()))
            .map(MessageItem::getMessage)
            .findFirst();
    }
    
    /**
     * 모든 메시지 정보를 반환합니다.
     */
    public List<MessageItem> getItems() {
        return items;
    }
} 