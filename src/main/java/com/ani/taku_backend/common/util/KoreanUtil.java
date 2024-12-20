package com.ani.taku_backend.common.util;

public class KoreanUtil {

    /**
     * 한글 초성 추출
     * @param text
     * @return
     */
    public static String getChosung(String text) {
        String[] chosung = {"ㄱ","ㄲ","ㄴ","ㄷ","ㄸ","ㄹ","ㅁ","ㅂ","ㅃ","ㅅ","ㅆ","ㅇ","ㅈ","ㅉ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ"};
        StringBuilder result = new StringBuilder();
        
        for (char ch : text.toCharArray()) {
            if (ch >= '가' && ch <= '힣') {
                int code = (ch - '가') / 588;
                result.append(chosung[code]);
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
}
