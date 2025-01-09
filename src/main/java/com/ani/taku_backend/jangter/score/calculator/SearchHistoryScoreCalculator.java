package com.ani.taku_backend.jangter.score.calculator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.vo.UserSearchHistory;
import com.ani.taku_backend.jangter.score.ScoreCalculator;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SearchHistoryScoreCalculator implements ScoreCalculator<UserSearchHistory> {

    @Override
    public double calculate(DuckuJangter recommendProduct, List<String> recommendProductKeywords, UserSearchHistory userSearchHistory) {
        double searchHistoryScore = 0.0;

        // 카테고리 50% 점수 부여
        // 1. 카테고리 유사도 (0.4)
        if(userSearchHistory.getCategoryIds().contains(recommendProduct.getItemCategories().getId())) {
            searchHistoryScore += 0.5;
        }

        // 키워드 50% 점수 부여
        List<String> userSearchKeywords = userSearchHistory.getKeywords();

        if(!recommendProductKeywords.isEmpty() && !userSearchKeywords.isEmpty()) {
            // 두 키워드 리스트 간의 공통 요소 찾기
            Set<String> commonKeywords = new HashSet<>(recommendProductKeywords);
            commonKeywords.retainAll(userSearchKeywords);

        if (!commonKeywords.isEmpty()) {
            // 공통 키워드가 있으면 점수 부여
            double similarity = (double) commonKeywords.size() / 
                    Math.max(recommendProductKeywords.size(), userSearchKeywords.size());
                searchHistoryScore += (0.5 * similarity);
            }
        }
        return searchHistoryScore;
    }
}