package com.ani.taku_backend.chatroom.domain.document;

import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import lombok.Getter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * 채팅방 참여자 정보 모음을 관리하는 클래스
 * 여러 참여자들의 정보를 맵 형태로 관리합니다.
 */
@Getter
public class Participants {
    private Map<Long, ParticipantInfo> info = new ConcurrentHashMap<>();


    public Participants() {
    }

    public void addParticipant(Long userId, JangterChatRole role) {
        info.put(userId, new ParticipantInfo(userId, role));
    }

    public synchronized void updateMessageStock(Long userId, boolean increase) {
        ParticipantInfo info = this.info.get(userId);
        if (info != null) {
            if (increase) {
                info.plusMessage();
            } else {
                info.resetMessageStock();
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

    /*
    모든 참가자가 연결되지 않은 상태인지(방을 나간 상태인지) 확인하는 함수
    * */
    public boolean allParticipantsInactive() {
        return info.values().stream().allMatch(p -> !p.getIsConnected());
    }

    /*
     * 참가자 참여 정보를 false로 만드는 비활성화 함수
     * */
    public synchronized void setDisconnected(Long userId) {
        ParticipantInfo participant = info.get(userId);
        if (participant != null) {
            participant.disconnect();
        }
    }

    public boolean containsUser(Long userId) {
        return info.containsKey(userId);
    }

    /**
     * 모든 참여자를 조회합니다.
     *
     * @return 참여자 ID 목록
     */
    public List<Long> getAllParticipantIds() {
        return new ArrayList<>(info.keySet());
    }

    /**
     * 판매자 참여자를 찾습니다.
     *
     * @return 판매자 ID와 정보
     */
    public Map.Entry<Long, ParticipantInfo> findSeller() {
        return info.entrySet().stream()
                .filter(entry -> entry.getValue().isSeller())
                .findFirst()
                .orElse(null);
    }

    /**
     * 구매자 참여자를 찾습니다.
     *
     * @return 구매자 ID와 정보
     */
    public Map.Entry<Long, ParticipantInfo> findBuyer() {
        return info.entrySet().stream()
                .filter(entry -> entry.getValue().isBuyer())
                .findFirst()
                .orElse(null);
    }

    /**
     * 판매자 ID를 조회합니다.
     * 
     * @deprecated 현재 사용되지 않음, 필요한 경우 findSeller() 사용 권장
     * @return 판매자 ID
     */
    @Deprecated
    public Long getSellerId() {
        Map.Entry<Long, ParticipantInfo> seller = findSeller();
        return seller != null ? seller.getKey() : null;
    }

    /**
     * 구매자 ID를 조회합니다.
     * 
     * @deprecated 현재 사용되지 않음, 필요한 경우 findBuyer() 사용 권장
     * @return 구매자 ID
     */
    @Deprecated
    public Long getBuyerId() {
        Map.Entry<Long, ParticipantInfo> buyer = findBuyer();
        return buyer != null ? buyer.getKey() : null;
    }

    /**
     * 특정 사용자를 연결 상태로 설정합니다.
     * 
     * @deprecated 향후 ParticipantSyncService 사용 권장
     * @param userId 사용자 ID
     */
    @Deprecated
    public void setConnected(Long userId) {
        ParticipantInfo participant = info.get(userId);
        if (participant != null) {
            participant.connect();
        }
    }

    /**
     * 모든 참여자가 연결 해제 상태인지 확인합니다.
     *
     * @return 모든 참여자가 연결 해제 상태인 경우 true
     */
    public boolean isAllDisconnected() {
        return info.values().stream()
                .allMatch(participant -> participant.getIsConnected() == null || !participant.getIsConnected());
    }
}