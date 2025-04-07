package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.contansts.MessageConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 채팅방별 메타 정보를 표현하는 Value Object
 */
public class ChatRoomMetaInfos {
    
    private final List<MetaInfoItem> items;

    public static class MetaInfoItem {
        private final Long chatRoomId;
        private final ChatRoomMetaInfo metaInfo;
        
        private MetaInfoItem(Long chatRoomId, ChatRoomMetaInfo metaInfo) {
            this.chatRoomId = Objects.requireNonNull(chatRoomId, MessageConstants.CHAT_ROOM_ID_NOT_NULL);
            this.metaInfo = metaInfo; // metaInfo는 null 허용
        }
        
        public Long getChatRoomId() {
            return chatRoomId;
        }
        
        public ChatRoomMetaInfo getMetaInfo() {
            return metaInfo;
        }
    }
    
    private ChatRoomMetaInfos(List<MetaInfoItem> items) {
        this.items = Collections.unmodifiableList(
            items != null ? new ArrayList<>(items) : new ArrayList<>()
        );
    }
    

    private static ChatRoomMetaInfos fromMetaInfoCollection(Collection<ChatRoomMetaInfo> metaInfos) {
        if (metaInfos == null || metaInfos.isEmpty()) {
            return empty();
        }
        
        List<MetaInfoItem> items = metaInfos.stream()
            .filter(metaInfo -> metaInfo != null && metaInfo.getChatRoomId() != null)
            .map(metaInfo -> new MetaInfoItem(metaInfo.getChatRoomId(), metaInfo))
            .collect(Collectors.toList());
        
        return new ChatRoomMetaInfos(items);
    }

    public static ChatRoomMetaInfos empty() {
        return new ChatRoomMetaInfos(List.of());
    }
    

    public static ChatRoomMetaInfos of(Collection<ChatRoomMetaInfo> metaInfos) {
        return fromMetaInfoCollection(metaInfos);
    }

    public Optional<ChatRoomMetaInfo> getMetaInfo(Long chatRoomId) {
        if (chatRoomId == null) {
            return Optional.empty();
        }
        
        return items.stream()
            .filter(item -> chatRoomId.equals(item.getChatRoomId()))
            .map(MetaInfoItem::getMetaInfo)
            .findFirst();
    }

    public List<MetaInfoItem> getItems() {
        return items;
    }
} 