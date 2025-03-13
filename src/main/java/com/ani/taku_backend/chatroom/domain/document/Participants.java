package com.ani.taku_backend.chatroom.domain.document;

import com.ani.taku_backend.chatroom.domain.constant.MarketRole;
import lombok.Getter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Getter
public class Participants {
    private Map<Long, ParticipantInfo> info = new ConcurrentHashMap<>();


    public Participants() {
    }

    public void addParticipant(Long userId, MarketRole role) {
        info.put(userId, new ParticipantInfo(userId, role));
    }

    public synchronized void updateMessageStock(Long userId, boolean increase) {
        ParticipantInfo info = this.info.get(userId);
        if (info != null) {
            if (increase) info.plusMessage();
            else info.resetMessageStock();
        }
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
                .filter(entry -> entry.getValue().getRole() == MarketRole.SELLER)
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
                .filter(entry -> entry.getValue().getRole() == MarketRole.BUYER)
                .findFirst()
                .orElse(null);
    }

    /**
     * 판매자 ID를 조회합니다.
     *
     * @return 판매자 ID
     */
    public Long getSellerId() {
        Map.Entry<Long, ParticipantInfo> seller = findSeller();
        return seller != null ? seller.getKey() : null;
    }

    /**
     * 구매자 ID를 조회합니다.
     *
     * @return 구매자 ID
     */
    public Long getBuyerId() {
        Map.Entry<Long, ParticipantInfo> buyer = findBuyer();
        return buyer != null ? buyer.getKey() : null;
    }

    /**
     * 특정 사용자를 연결 상태로 설정합니다.
     *
     * @param userId 사용자 ID
     */
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