package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.document.ParticipantInfo;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomMetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 채팅방 메타정보 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomMetaService {

    private final ChatRoomMetaRepository chatRoomMetaRepository;

    /**
     * 사용자가 참여한 활성 채팅방 메타 정보를 조회합니다.
     */
    public List<ChatRoomMetaInfo> getConnectedChatRoomMetaInfos(Long userId) {
        List<ChatRoomMetaInfo> userChatRoomMetaInfos = chatRoomMetaRepository
                .findByParticipantsUserId(userId);
        
        if (userChatRoomMetaInfos.isEmpty()) {
            return Collections.emptyList();
        }
        
        return userChatRoomMetaInfos.stream()
                .filter(metaInfo -> metaInfo.getParticipants().getInfo().values().stream()
                        .anyMatch(participant -> participant.getIsConnected() != null
                                && participant.getIsConnected()))
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 ID 목록과 사용자 ID에 해당하는 안읽은 메시지 개수 맵을 생성합니다.
     */
    public Map<Long, Integer> createUnreadCountMap(List<Long> chatRoomIds, Long userId) {
        if (chatRoomIds == null || chatRoomIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<Long, Integer> unreadCountMap = new HashMap<>();
        
        List<ChatRoomMetaInfo> metaInfos = chatRoomMetaRepository.findByChatRoomIdIn(chatRoomIds);
        
        // 각 채팅방의 안읽은 메시지 개수를 도메인 모델에서 조회
        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            unreadCountMap.put(metaInfo.getChatRoomId(), metaInfo.getUnreadCount(userId));
        }
        
        // 모든 채팅방 ID에 대해 결과가 있는지 확인하고 없으면 0 추가
        for (Long chatRoomId : chatRoomIds) {
            unreadCountMap.putIfAbsent(chatRoomId, 0);
        }
        
        return unreadCountMap;
    }

    /**
     * 사용자의 모든 채팅방의 안읽은 메시지 총 개수를 계산합니다.
     */
    public Integer getTotalUnreadCount(Long userId) {
        List<ChatRoomMetaInfo> userChatrooms = chatRoomMetaRepository
                .findByParticipantIdOrderByUpdateAtDesc(userId.toString());

        // 도메인 모델의 static 메서드 활용
        return ChatRoomMetaInfo.calculateTotalUnreadCount(userChatrooms, userId);
    }
} 