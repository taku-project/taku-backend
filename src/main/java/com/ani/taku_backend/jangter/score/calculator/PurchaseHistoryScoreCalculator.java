package com.ani.taku_backend.jangter.score.calculator;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.vo.UserPurchaseHistory;
import com.ani.taku_backend.jangter.score.ScoreCalculator;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PurchaseHistoryScoreCalculator implements ScoreCalculator<UserPurchaseHistory> {

    @Override
    public double calculate(DuckuJangter recommendProduct, List<String> recommendProductKeywords, UserPurchaseHistory history) {
        double purchaseHistoryScore = 0.0;  

        if(history == null){
            return purchaseHistoryScore;
        }


        // 구매 스코어 (0.3)
        if(history.getCategoryIds().contains(recommendProduct.getItemCategories().getId())){
            purchaseHistoryScore += 0.3;
        }

        // 키워드 스코코어 (0.5)
        List<String> purchaseProductTitleKeywords = history.getKeywords();
        Set<String> commonKeywords = new HashSet<>(purchaseProductTitleKeywords);
        commonKeywords.retainAll(recommendProductKeywords);

        if (!commonKeywords.isEmpty()) {
            double similarity = (double) commonKeywords.size() /
                Math.max(recommendProductKeywords.size(), purchaseProductTitleKeywords.size());
            purchaseHistoryScore += (0.5 * similarity);
        }

        // 가격 스코어 (0.2)
        BigDecimal productPrice = recommendProduct.getPrice();
        if (productPrice.compareTo(history.getMinPrice()) >= 0 &&
            productPrice.compareTo(history.getMaxPrice()) <= 0) {
            purchaseHistoryScore += 0.2;  // 가격 범위 내에 있으면 만점
        }
        return purchaseHistoryScore;
    }
}