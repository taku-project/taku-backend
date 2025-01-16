package com.ani.taku_backend.jangter.score;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import java.util.List;

public interface ScoreCalculator<T> {
    double calculate(DuckuJangter recommendProduct, List<String> recommendProductKeywords, T userHistory);
}
