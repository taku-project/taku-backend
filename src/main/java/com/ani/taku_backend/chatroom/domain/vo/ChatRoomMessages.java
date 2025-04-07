package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.contansts.MessageConstants;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.dto.response.ChatMessageResponseDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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


    public static ChatRoomMessages empty() {
        return new ChatRoomMessages(List.of());
    }

    public static ChatRoomMessages of(Long chatRoomId, ChatMessage lastMessage) {
        if (chatRoomId == null) {
            return empty();
        }
        
        return new ChatRoomMessages(List.of(new MessageItem(chatRoomId, lastMessage)));
    }
    

    public static ChatRoomMessages of(List<MessageItem> items) {
        return new ChatRoomMessages(items);
    }


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


    public Optional<ChatMessage> getLastMessage(Long chatRoomId) {
        if (chatRoomId == null || items.isEmpty()) {
            return Optional.empty();
        }
        
        return items.stream()
            .filter(Objects::nonNull)
            .filter(item -> chatRoomId.equals(item.getChatRoomId()))
            .map(MessageItem::getMessage)
            .filter(Objects::nonNull)
            .findFirst();
    }

    /**
     * 채팅방의 마지막 메시지를 DTO로 변환.
     *
     * @param chatRoom 채팅방 객체
     * @param metaInfo 채팅방 메타 정보
     * @param users 채팅방 사용자 정보
     * @return 마지막 메시지 DTO, 없거나 조건을 만족하지 않으면 null
     */
    public ChatMessageResponseDTO createLastMessageDTO(
            ChatRoom chatRoom,
            ChatRoomMetaInfo metaInfo,
            ChatRoomUsers users) {
            
        Long chatRoomId = chatRoom.getId();
        if (chatRoomId == null) {
            return null;
        }
        
        // 1. 마지막 메시지 조회 (현재 객체에서 먼저 찾고, 없으면 metaInfo에서 찾음)
        return getLastMessage(chatRoomId)
                .or(() -> Optional.ofNullable(metaInfo.getLastMessage()))
                .filter(msg -> msg.getSenderId() != null)
                .map(lastMessage -> {
                    // 2. 발신자 이름 획득
                    String senderName = users.getUserNicknameOrUnknown(lastMessage.getSenderId());
                    
                    // 3. DTO 생성 및 반환
                    return ChatMessageResponseDTO.from(lastMessage, senderName, chatRoom.getWsRoomId());
                })
                .orElse(null);
    }

    public List<MessageItem> getItems() {
        return items;
    }
} 