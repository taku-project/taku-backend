package com.ani.taku_backend.jangter.score.calculator;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.vo.UserBookmarkHistory;

import lombok.extern.slf4j.Slf4j;

import com.ani.taku_backend.jangter.score.ScoreCalculator;

@Component
@Slf4j
public class BookmarkScoreCalculator implements ScoreCalculator<UserBookmarkHistory> {

    @Override
    public double calculate(DuckuJangter recommendProduct, List<String> recommendProductKeywords, UserBookmarkHistory history) {
        double bookmarkScore = 0.0;
         // 찜 목록 점수 (20%)
        if(history == null){
            return bookmarkScore;
        }


         if(history.getCategoryIds().contains(recommendProduct.getItemCategories().getId())){
            bookmarkScore += 0.2;
        }

        // 키워드 스코어 (0.5)
        List<String> purchaseProductTitleKeywords = history.getKeywords();
        Set<String> commonKeywords = new HashSet<>(purchaseProductTitleKeywords);
        commonKeywords.retainAll(recommendProductKeywords);

        if (!commonKeywords.isEmpty()) {
            double similarity = (double) commonKeywords.size() / 
                Math.max(recommendProductKeywords.size(), purchaseProductTitleKeywords.size());
            bookmarkScore += (0.5 * similarity);
        }

        // 가격 스코어 (0.2)
        BigDecimal productPrice = recommendProduct.getPrice();
        if (productPrice.compareTo(history.getMinPrice()) >= 0 && 
            productPrice.compareTo(history.getMaxPrice()) <= 0) {
            bookmarkScore += 0.2;  // 가격 범위 내에 있으면 만점
        }
        return bookmarkScore;
    }
}