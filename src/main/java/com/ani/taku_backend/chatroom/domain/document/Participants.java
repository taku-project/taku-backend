package com.ani.taku_backend.chatroom.domain.document;

import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import lombok.Getter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * 채팅방 참여자 정보 모음을 관리하는 클래스
 * 여러 참여자들의 정보를 맵 형태로 관리합니다.
 */
@Getter
public class Participants {
    private Map<Long, ParticipantInfo> info = new ConcurrentHashMap<>();

    /**
     * 새로운 참여자 정보 컬렉션을 생성합니다.
     */
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
     * 특정 사용자의 메시지 스톡을 업데이트합니다.
     *
     * @param userId 사용자 ID
     * @param increase true면 증가, false면 초기화
     */
    public synchronized void updateMessageStock(Long userId, boolean increase) {
        ParticipantInfo participant = this.info.get(userId);
        if (participant != null) {
            if (increase) {
                participant.plusMessage();
            } else {
                participant.resetMessageStock();
            }
        }
    }
    
    /**
     * 특정 사용자의 안 읽은 메시지 수를 가져옵니다.
     *
     * @param userId 사용자 ID
     * @return 안 읽은 메시지 수 (사용자가 없는 경우 null)
     */
    public Long getUnreadCount(Long userId) {
        ParticipantInfo participant = info.get(userId);
        return participant != null ? participant.getMessageStock().longValue() : null;
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
     * 특정 참가자를 활성화 상태로 설정합니다.
     *
     * @param userId 사용자 ID
     */
    public synchronized void activateUser(Long userId) {
        ParticipantInfo participant = info.get(userId);
        if (participant != null) {
            participant.activate();
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
     * 모든 참여자 ID 목록을 반환합니다.
     *
     * @return 참여자 ID 목록
     */
    public List<Long> getAllParticipantIds() {
        return new ArrayList<>(info.keySet());
    }

    /**
     * 판매자 참여자를 찾습니다.
     *
     * @return 판매자 ID와 정보가 담긴 Optional
     */
    public Optional<Map.Entry<Long, ParticipantInfo>> findSeller() {
        return info.entrySet().stream()
                .filter(entry -> entry.getValue().isSeller())
                .findFirst();
    }

    /**
     * 구매자 참여자를 찾습니다.
     *
     * @return 구매자 ID와 정보가 담긴 Optional
     */
    public Optional<Map.Entry<Long, ParticipantInfo>> findBuyer() {
        return info.entrySet().stream()
                .filter(entry -> entry.getValue().isBuyer())
                .findFirst();
    }

    /**
     * 판매자 ID를 조회합니다.
     *
     * @return 판매자 ID (없는 경우 null)
     */
    public Long getSellerId() {
        return findSeller()
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * 구매자 ID를 조회합니다.
     *
     * @return 구매자 ID (없는 경우 null)
     */
    public Long getBuyerId() {
        return findBuyer()
                .map(Map.Entry::getKey)
                .orElse(null);
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
}