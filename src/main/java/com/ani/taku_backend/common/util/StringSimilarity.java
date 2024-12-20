package com.ani.taku_backend.common.util;

public class StringSimilarity {
    public static final double SIMILARITY_THRESHOLD = 0.6; // 유사도 임계값

    /**
     * 한글에 최적화된 문자열 유사도 계산
     * 자음/모음 분리 및 초성 매칭을 고려합니다
     */
    public static double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }

        // 한글 자모음 분리 처리
        String decomposed1 = decomposeHangul(s1);
        String decomposed2 = decomposeHangul(s2);
        
        // 초성만 추출한 문자열의 유사도도 계산에 반영
        String chosung1 = extractChosung(s1);
        String chosung2 = extractChosung(s2);

        // 완성형 한글 유사도 (0.6 가중치)
        double fullSimilarity = calculateLevenshteinSimilarity(s1, s2) * 0.6;
        // 자모 분리 유사도 (0.3 가중치)
        double decomposedSimilarity = calculateLevenshteinSimilarity(decomposed1, decomposed2) * 0.3;
        // 초성 유사도 (0.1 가중치)
        double chosungSimilarity = calculateLevenshteinSimilarity(chosung1, chosung2) * 0.1;

        return fullSimilarity + decomposedSimilarity + chosungSimilarity;
    }

    /**
     * 레벤슈타인 거리 기반 유사도 계산
     */
    private static double calculateLevenshteinSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else if (j > 0) {
                    int newValue = costs[j - 1];
                    if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                        newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                    }
                    costs[j - 1] = lastValue;
                    lastValue = newValue;
                }
            }
            if (i > 0) {
                costs[s2.length()] = lastValue;
            }
        }

        return 1.0 - ((double) costs[s2.length()] / Math.max(s1.length(), s2.length()));
    }

    /**
     * 한글 문자열을 자모음으로 분리
     */
    private static String decomposeHangul(String text) {
        StringBuilder result = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (ch >= '가' && ch <= '힣') {
                int unicode = ch - '가';
                char chosung = (char) ('ㄱ' + unicode / (21 * 28));
                char jungsung = (char) ('ㅏ' + (unicode % (21 * 28)) / 28);
                char jongsung = (char) ('ㄱ' + unicode % 28 - 1);
                
                result.append(chosung);
                result.append(jungsung);
                if (jongsung >= 'ㄱ') {
                    result.append(jongsung);
                }
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * 한글 문자열에서 초성만 추출
     */
    private static String extractChosung(String text) {
        StringBuilder result = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (ch >= '가' && ch <= '힣') {
                int unicode = ch - '가';
                char chosung = (char) ('ㄱ' + unicode / (21 * 28));
                result.append(chosung);
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * 두 문자열이 유사한지 검사합니다
     * @param s1 첫 번째 문자열
     * @param s2 두 번째 문자열
     * @return 유사도가 임계값 이상이면 true
     */
    public static boolean isSimilar(String s1, String s2) {
        return calculateSimilarity(s1, s2) >= SIMILARITY_THRESHOLD;
    }
} 
