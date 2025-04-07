package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import java.util.List;

public interface ChatRoomMetaRepositoryCustom {
    
    /**
     * 여러 채팅방의 메타 정보와 마지막 메시지를 한 번에 조회합니다.
     */
    List<ChatRoomMetaInfo> findMetaInfoWithLastMessages(List<Long> chatRoomIds);

} 