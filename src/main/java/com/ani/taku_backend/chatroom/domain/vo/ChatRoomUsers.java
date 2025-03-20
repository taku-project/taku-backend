package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.user.model.entity.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class ChatRoomUsers {
    
    private final Map<Long, User> userById;

    public ChatRoomUsers(Collection<User> users) {
        Map<Long, User> userMap = new HashMap<>();
        for (User user : users) {
            if (user != null && user.getUserId() != null) {
                userMap.put(user.getUserId(), user);
            }
        }
        this.userById = Collections.unmodifiableMap(userMap);
    }
    

    public static ChatRoomUsers of(User buyer, User seller) {
        return new ChatRoomUsers(Arrays.asList(buyer, seller));
    }
    

    public static ChatRoomUsers fromChatRooms(List<ChatRoom> chatRooms) {
        Map<Long, User> userMap = new HashMap<>();
        
        for (ChatRoom chatRoom : chatRooms) {
            User buyer = chatRoom.getBuyer();
            User seller = chatRoom.getSeller();
            
            if (buyer != null && buyer.getUserId() != null) {
                userMap.put(buyer.getUserId(), buyer);
            }
            
            if (seller != null && seller.getUserId() != null) {
                userMap.put(seller.getUserId(), seller);
            }
        }
        
        return new ChatRoomUsers(userMap.values());
    }
    
    /**
     * 단일 채팅방에서 참여자 정보를 추출하여 ChatRoomUsers 객체를 생성합니다.
     */
    public static ChatRoomUsers fromChatRoom(ChatRoom chatRoom) {
        Map<Long, User> userMap = new HashMap<>();
        
        User buyer = chatRoom.getBuyer();
        User seller = chatRoom.getSeller();
        
        if (buyer != null && buyer.getUserId() != null) {
            userMap.put(buyer.getUserId(), buyer);
        }
        
        if (seller != null && seller.getUserId() != null) {
            userMap.put(seller.getUserId(), seller);
        }
        
        return new ChatRoomUsers(userMap.values());
    }
    
    /**
     * 사용자 ID로 사용자 정보를 조회합니다.
     */
    public Optional<User> getUser(Long userId) {
        return Optional.ofNullable(userById.get(userId));
    }
    
    /**
     * 사용자 ID로 사용자 닉네임을 조회합니다.
     * 사용자가 없는 경우 기본값을 반환합니다.
     */
    public String getUserNickname(Long userId, String defaultValue) {
        User user = userById.get(userId);
        return user != null ? user.getNickname() : defaultValue;
    }
    
    /**
     * 사용자 ID로 프로필 이미지 URL을 조회합니다.
     */
    public String getUserProfileImage(Long userId) {
        User user = userById.get(userId);
        return user != null ? user.getProfileImg() : null;
    }

    
    /**
     * 저장된 모든 사용자 수를 반환합니다.
     */
    public int size() {
        return userById.size();
    }

} 