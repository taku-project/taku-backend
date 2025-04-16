package com.ani.taku_backend.chatroom.domain.document;

import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import lombok.Getter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 채팅방 참여자 정보 모음을 관리하는 클래스
 */
@Getter
public class Participants {

    private Map<Long, ParticipantInfo> info = new ConcurrentHashMap<>();

    public Participants() {
    }

    /**
     * 새로운 참여자를 추가합니다.
     *
     * @param userId 사용자 ID
     * @param role 사용자 역할
     */
    public void addParticipant(Long userId, JangterChatRole role) {
        info.put(userId, new ParticipantInfo(userId, role));
    }

    /**
     * 모든 참가자가 비활성화 상태인지 확인합니다.
     *
     * @return 모든 참가자가 비활성화 상태이면 true
     */
    public boolean areAllParticipantsInactive() {
        return info.values().stream().allMatch(p -> !p.getIsActive());
    }

    /**
     * 특정 참가자를 비활성화 상태로 설정합니다.
     *
     * @param userId 사용자 ID
     */
    public synchronized void deactivateUser(Long userId) {
        ParticipantInfo participant = info.get(userId);
        if (participant != null) {
            participant.deactivate();
        }
    }


    /**
     * 특정 사용자가 참가자 목록에 포함되어 있는지 확인합니다.
     *
     * @param userId 사용자 ID
     * @return 포함되어 있으면 true
     */
    public boolean containsUser(Long userId) {
        return info.containsKey(userId);
    }



    /**
     * 모든 참여자가 비활성화 상태인지 확인합니다.
     *
     * @return 모든 참여자가 비활성화 상태인 경우 true
     */
    public boolean isAllInactive() {
        return info.values().stream()
                .allMatch(participant -> participant.getIsActive() == null || !participant.getIsActive());
    }

    /**
     * 특정 사용자가 특정 역할을 가지고 있는지 확인합니다.
     *
     * @param userId 확인할 사용자 ID
     * @param role 확인할 역할
     * @return 사용자가 해당 역할을 가지고 있으면 true, 그렇지 않으면 false
     */
    public boolean hasUserWithRole(Long userId, JangterChatRole role) {
        ParticipantInfo participant = info.get(userId);
        if (participant == null) {
            return false;
        }
        
        return (role == JangterChatRole.BUYER && participant.isBuyer()) ||
               (role == JangterChatRole.SELLER && participant.isSeller());
    }

    /**
     * 특정 사용자가 활성 상태인지 확인합니다.
     *
     * @param userId 확인할 사용자 ID
     * @return 사용자가 활성 상태면 true, 그렇지 않으면 false
     */
    public boolean isParticipantActive(Long userId) {
        ParticipantInfo participant = info.get(userId);
        if (participant == null) {
            return false;
        }
        
        return participant.isActive();
    }

    /**
     * 특정 참가자를 활성화 상태로 설정합니다.
     *
     * @param userId 사용자 ID
     * @return 상태가 변경되었으면 true, 사용자가 없거나 이미 활성화 상태면 false
     */
    public synchronized boolean activateParticipant(Long userId) {
        ParticipantInfo participant = info.get(userId);
        if (participant == null || participant.isActive()) {
            return false;
        }
        participant.activate();
        return true;
    }
}