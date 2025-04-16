package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import java.util.List;

public interface ChatRoomMetaRepositoryCustom {
    
    /**
     * 여러 채팅방의 메타 정보와 마지막 메시지를 한 번에 조회합니다.
     */
    List<ChatRoomMetaInfo> findMetaInfoWithLastMessages(List<Long> chatRoomIds);

    /**
     * 채팅방의 특정 참여자를 재활성화합니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 업데이트 성공 여부
     */
    boolean reactivateParticipant(Long chatRoomId, Long userId);

} 