package com.ani.taku_backend.chatroom.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 날짜 및 시간 포맷팅 유틸리티 클래스
 */
public class ChatDateTimeFormatter {

    private ChatDateTimeFormatter() {
    }

    /**
     * - 오늘: 오전/오후 시:분
     * - 어제: 어제 오전/오후 시:분
     * - 올해 내: MM월 dd일
     * - 작년 이전: yyyy년 MM월 dd일
     *
     * @param time 포맷팅할 LocalDateTime
     * @return 포맷팅된 시간 문자열, time이 null이면 null 반환
     */
    public static String formatMessageTime(LocalDateTime time) {
        if (time == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        LocalDate messageDate = time.toLocalDate();

        // 시간 형식 (오전/오후 시:분)
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("a h:mm");
        String timeStr = time.format(timeFormatter)
                .replace("AM", "오전")
                .replace("PM", "오후");

        // 오늘 보낸 메시지
        if (messageDate.equals(today)) {
            return timeStr;
        }

        // 어제 보낸 메시지
        if (messageDate.equals(today.minusDays(1))) {
            return "어제 " + timeStr;
        }

        // 올해 보낸 메시지
        if (messageDate.getYear() == today.getYear()) {
            return time.format(java.time.format.DateTimeFormatter.ofPattern("M월 d일"));
        }

        // 작년 이전 메시지
        return time.format(java.time.format.DateTimeFormatter.ofPattern("yyyy년 M월 d일"));
    }
}