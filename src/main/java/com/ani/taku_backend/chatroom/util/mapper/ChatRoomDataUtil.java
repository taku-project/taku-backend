package com.ani.taku_backend.chatroom.util.mapper;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.document.ParticipantInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 채팅방 관련 데이터 처리를 위한 유틸리티 클래스
 */
public class ChatRoomDataUtil {

    /**
     * 채팅방 메타 정보 목록에서 채팅방 ID 목록을 추출합니다.
     */
    public static List<Long> extractChatRoomIds(List<ChatRoomMetaInfo> chatRoomMetaInfos) {
        return chatRoomMetaInfos.stream()
                .map(ChatRoomMetaInfo::getChatRoomId)
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 메타 정보 목록에서 참여자 ID 목록을 추출합니다.
     */
    public static List<Long> extractParticipantIds(List<ChatRoomMetaInfo> chatRoomMetaInfos) {
        return chatRoomMetaInfos.stream()
                .flatMap(metaInfo -> metaInfo.getParticipants().getInfo().values().stream()
                        .map(ParticipantInfo::getUserId))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 ID 목록과 사용자 ID에 해당하는 안읽은 메시지 개수 맵을 생성합니다.
     */
    public static Map<Long, Integer> createUnreadCountMap(List<Long> chatRoomIds, Long userId, Map<Long, ChatRoomMetaInfo> chatRoomMetaInfoMap) {
        Map<Long, Integer> unreadCountMap = new HashMap<>();
        for (Long chatRoomId : chatRoomIds) {
            ChatRoomMetaInfo metaInfo = chatRoomMetaInfoMap.get(chatRoomId);
            if (metaInfo != null && metaInfo.getParticipants() != null) {
                ParticipantInfo participantInfo = metaInfo.getParticipants().getInfo().get(userId);
                unreadCountMap.put(chatRoomId, participantInfo != null ? participantInfo.getMessageStock() : 0);
            } else {
                unreadCountMap.put(chatRoomId, 0);
            }
        }
        return unreadCountMap;
    }

    /**
     * 채팅 메시지 목록에서 각 채팅방의 마지막 메시지 맵을 생성합니다.
     */
    public static Map<Long, ChatMessage> createLastMessageMap(List<ChatMessage> latestMessages) {
        Map<Long, ChatMessage> lastMessageMap = new HashMap<>();
        for (ChatMessage message : latestMessages) {
            if (!lastMessageMap.containsKey(message.getChatRoomId())) {
                lastMessageMap.put(message.getChatRoomId(), message);
            }
        }
        return lastMessageMap;
    }
} 