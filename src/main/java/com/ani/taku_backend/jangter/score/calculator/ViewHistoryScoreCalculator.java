package com.ani.taku_backend.jangter.score.calculator;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ani.taku_backend.common.service.ExtractKeywordService;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.vo.UserSearchHistory;
import com.ani.taku_backend.jangter.score.ScoreCalculator;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ViewHistoryScoreCalculator implements ScoreCalculator<UserSearchHistory> {

    @Override
    public double calculate(DuckuJangter recommendProduct, List<String> recommendProductKeywords, UserSearchHistory userHistory) {
        double itemScore = 0.0;
        // 1. 카테고리 유사도 (0.5)
        if(userHistory.getCategoryIds().contains(recommendProduct.getItemCategories().getId())) {
            itemScore += 0.5;
        }

        // extractKeywordService
        // 2. 제목 키워드 유사도 (0.5)
        List<String> userViewProductKeywords = userHistory.getKeywords();
        if(!recommendProductKeywords.isEmpty() && !userViewProductKeywords.isEmpty()) {
            // 교집합
            Set<String> commonKeywords = new HashSet<>(recommendProductKeywords);
            commonKeywords.retainAll(userViewProductKeywords);


            double keywordSimilarity = (double) commonKeywords.size() /
                Math.max(recommendProductKeywords.size(), userViewProductKeywords.size());
            itemScore += (0.5 * keywordSimilarity);
        }
        return itemScore;
    }
}