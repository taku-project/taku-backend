package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.user.model.entity.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 채팅방 사용자 정보를 표현하는 Value Object
 */
public class ChatRoomUsers {
    
    private final List<ChatRoomUserItem> users;
    private final String UNKNOWN = "알 수 없음";


    public static class ChatRoomUserItem {
        private final Long userId;
        private final String nickname;
        private final String profileImg;
        
        private ChatRoomUserItem(Long userId, String nickname, String profileImg) {
            this.userId = Objects.requireNonNull(userId, "사용자 ID는 null일 수 없습니다");
            this.nickname = nickname != null ? nickname : UNKNOWN;
            this.profileImg = profileImg; // profileImg는 null 허용
        }
        
        private static final String UNKNOWN = "알 수 없음";
        
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

    public static ChatRoomUsers empty() {
        return new ChatRoomUsers(List.of());
    }

    public static ChatRoomUsers of(User buyer, User seller) {
        return fromUserEntities(Arrays.asList(buyer, seller));
    }

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

    public Optional<ChatRoomUserItem> getUser(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        
        return users.stream()
            .filter(user -> userId.equals(user.getUserId()))
            .findFirst();
    }

    public String getUserNicknameOrUnknown(Long userId) {
        return getUser(userId)
            .map(ChatRoomUserItem::getNickname)
            .orElse(UNKNOWN);
    }

    public String getUserProfileImage(Long userId) {
        return getUser(userId)
            .map(ChatRoomUserItem::getProfileImg)
            .orElse(null);
    }

} 