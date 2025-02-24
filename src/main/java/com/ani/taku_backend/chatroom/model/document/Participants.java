package com.ani.taku_backend.chatroom.model.document;

import com.ani.taku_backend.chatroom.model.constant.ParticipantRole;
import lombok.Getter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Getter
public class Participants {
    private Map<Long, ParticipantInfo> info = new ConcurrentHashMap<>();

    public void addParticipant(Long userId, ParticipantRole role) {
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
            participant.disconnected();
        }


    }

    public boolean containsUser(Long userId) {
        return info.containsKey(userId);
    }

}

