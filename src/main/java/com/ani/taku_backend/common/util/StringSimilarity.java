package com.ani.taku_backend.common.util;

public class StringSimilarity {
    public static final double SIMILARITY_THRESHOLD = 0.6; // 유사도 임계값

    /**
     * 두 문자열 간의 유사도를 계산합니다 (레벤슈타인 거리 기반)
     * @param s1 첫 번째 문자열
     * @param s2 두 번째 문자열
     * @return 0.0 ~ 1.0 사이의 유사도 (1에 가까울수록 유사)
     */
    public static double calculateSimilarity(String s1, String s2) {
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
     * 두 문자열이 유사한지 검사합니다
     * @param s1 첫 번째 문자열
     * @param s2 두 번째 문자열
     * @return 유사도가 임계값 이상이면 true
     */
    public static boolean isSimilar(String s1, String s2) {
        return calculateSimilarity(s1, s2) >= SIMILARITY_THRESHOLD;
    }
} 