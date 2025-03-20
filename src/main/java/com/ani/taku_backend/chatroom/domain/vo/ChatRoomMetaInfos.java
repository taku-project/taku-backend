package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class ChatRoomMetaInfos {
    
    private final Map<Long, ChatRoomMetaInfo> metaInfoByChatRoomId;
    

    private ChatRoomMetaInfos(Collection<ChatRoomMetaInfo> metaInfos) {
        Map<Long, ChatRoomMetaInfo> map = new HashMap<>();
        
        if (metaInfos != null) {
            for (ChatRoomMetaInfo metaInfo : metaInfos) {
                if (metaInfo != null && metaInfo.getChatRoomId() != null) {
                    map.put(metaInfo.getChatRoomId(), metaInfo);
                }
            }
        }
        
        this.metaInfoByChatRoomId = Collections.unmodifiableMap(map);
    }

    public static ChatRoomMetaInfos empty() {
        return new ChatRoomMetaInfos(List.of());
    }
    

    public static ChatRoomMetaInfos of(Collection<ChatRoomMetaInfo> metaInfos) {
        return new ChatRoomMetaInfos(metaInfos);
    }

    public Optional<ChatRoomMetaInfo> getMetaInfo(Long chatRoomId) {
        return Optional.ofNullable(metaInfoByChatRoomId.get(chatRoomId));
    }

} 