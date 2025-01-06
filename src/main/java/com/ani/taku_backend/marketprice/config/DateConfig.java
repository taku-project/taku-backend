package com.ani.taku_backend.marketprice.config;


import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DateConfig {
    @Value("${market-price.default-period-months:3}")
    private int defaultPeriodMonths;

    public LocalDate getDefaultStartDate() {
        return LocalDate.now().minusMonths(defaultPeriodMonths);
    }

    public LocalDate getDefaultEndDate() {
        return LocalDate.now();
    }
}