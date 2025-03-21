package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.contansts.MessageConstants;
import com.ani.taku_backend.user.model.entity.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 채팅방 사용자 정보를 표현하는 Value Object
 */
public class ChatRoomUsers {
    
    private final List<ChatRoomUserItem> users;

    /**
     * 사용자 정보를 표현하는 내부 클래스
     */
    public static class ChatRoomUserItem {
        private final Long userId;
        private final String nickname;
        private final String profileImg;
        
        private ChatRoomUserItem(Long userId, String nickname, String profileImg) {
            this.userId = Objects.requireNonNull(userId, MessageConstants.USER_ID_NOT_NULL);
            this.nickname = nickname != null ? nickname : MessageConstants.UNKNOWN_USER;
            this.profileImg = profileImg; // profileImg는 null 허용
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public String getNickname() {
            return nickname;
        }
        
        public String getProfileImg() {
            return profileImg;
        }
    }

    private ChatRoomUsers(List<ChatRoomUserItem> users) {
        this.users = Collections.unmodifiableList(
            users != null ? new ArrayList<>(users) : new ArrayList<>()
        );
    }
    
    /**
     * User 엔티티 컬렉션으로부터 ChatRoomUsers VO를 생성합니다.
     */
    public static ChatRoomUsers fromUserEntities(Collection<User> users) {
        if (users == null || users.isEmpty()) {
            return new ChatRoomUsers(List.of());
        }
        
        List<ChatRoomUserItem> items = users.stream()
            .filter(user -> user != null && user.getUserId() != null)
            .map(user -> new ChatRoomUserItem(
                user.getUserId(),
                user.getNickname(),
                user.getProfileImg()
            ))
            .collect(Collectors.toList());
        
        return new ChatRoomUsers(items);
    }
    
    /**
     * 빈 ChatRoomUsers 객체를 생성합니다.
     */
    public static ChatRoomUsers empty() {
        return new ChatRoomUsers(List.of());
    }

    /**
     * 구매자와 판매자 정보로 ChatRoomUsers 객체를 생성합니다.
     */
    public static ChatRoomUsers of(User buyer, User seller) {
        return fromUserEntities(Arrays.asList(buyer, seller));
    }
    
    /**
     * 기존 맵 데이터로부터 ChatRoomUsers 객체를 생성합니다.
     * 하위 호환성을 위해 제공됩니다.
     */
    public static ChatRoomUsers ofMap(Map<Long, User> userMap) {
        if (userMap == null || userMap.isEmpty()) {
            return empty();
        }
        
        return fromUserEntities(userMap.values());
    }
    
    /**
     * 채팅방 목록으로부터 사용자 정보를 추출하여 ChatRoomUsers 객체를 생성합니다.
     */
    public static ChatRoomUsers fromChatRooms(List<ChatRoom> chatRooms) {
        if (chatRooms == null || chatRooms.isEmpty()) {
            return empty();
        }
        
        List<User> users = new ArrayList<>();
        
        for (ChatRoom chatRoom : chatRooms) {
            User buyer = chatRoom.getBuyer();
            User seller = chatRoom.getSeller();
            
            if (buyer != null && buyer.getUserId() != null) {
                users.add(buyer);
            }
            
            if (seller != null && seller.getUserId() != null) {
                users.add(seller);
            }
        }
        
        return fromUserEntities(users);
    }
    
    /**
     * 단일 채팅방에서 참여자 정보를 추출하여 ChatRoomUsers 객체를 생성합니다.
     */
    public static ChatRoomUsers fromChatRoom(ChatRoom chatRoom) {
        if (chatRoom == null) {
            return empty();
        }
        
        List<User> users = new ArrayList<>();
        
        User buyer = chatRoom.getBuyer();
        User seller = chatRoom.getSeller();
        
        if (buyer != null && buyer.getUserId() != null) {
            users.add(buyer);
        }
        
        if (seller != null && seller.getUserId() != null) {
            users.add(seller);
        }
        
        return fromUserEntities(users);
    }
    
    /**
     * 사용자 ID로 사용자 정보를 조회합니다.
     */
    public Optional<ChatRoomUserItem> getUser(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        
        return users.stream()
            .filter(user -> userId.equals(user.getUserId()))
            .findFirst();
    }
    
    /**
     * 사용자 ID로 사용자 닉네임을 조회합니다.
     * 사용자가 없는 경우 "알 수 없음"을 반환합니다.
     */
    public String getUserNicknameOrUnknown(Long userId) {
        return getUser(userId)
            .map(ChatRoomUserItem::getNickname)
            .orElse(MessageConstants.UNKNOWN_USER);
    }
    
    /**
     * 사용자 ID로 프로필 이미지 URL을 조회합니다.
     */
    public String getUserProfileImage(Long userId) {
        return getUser(userId)
            .map(ChatRoomUserItem::getProfileImg)
            .orElse(null);
    }
    
    /**
     * 모든 사용자 정보를 반환합니다.
     */
    public List<ChatRoomUserItem> getUsers() {
        return users;
    }
} 