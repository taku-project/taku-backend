package com.ani.taku_backend.chatroom.domain.constant;

/**
 * 일반 채팅 및 그룹 채팅에서 사용자의 역할을 정의하는 Enum 클래스입니다.
 * OWNER: 채팅방 소유자/생성자
 * ADMIN: 채팅방 관리자 
 * MEMBER: 일반 참여자
 */
public enum ChatRole {
    OWNER,    // 채팅방 소유자/생성자
    ADMIN,    // 채팅방 관리자
    MEMBER;   // 일반 참여자
    
    /**
     * 해당 역할이 관리자 권한을 가진 역할인지 확인합니다.
     */
    public boolean hasAdminPrivilege() {
        return this == OWNER || this == ADMIN;
    }
} 