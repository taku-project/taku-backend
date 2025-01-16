package com.ani.taku_backend.marketprice.config;


import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DateConfig {
    @Value("${market-price.default-period-months:3}")
    private int defaultPeriodMonths;

    @Value("${market-price.date-format:yyyy-MM-dd-HH-mm}")
    private String dateFormat;

    private DateTimeFormatter formatter;

    @PostConstruct
    public void init() {
        formatter = DateTimeFormatter.ofPattern(dateFormat);
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    public LocalDate getDefaultStartDate() {
        return LocalDate.now().minusMonths(defaultPeriodMonths);
    }

    public LocalDate getDefaultEndDate() {
        return LocalDate.now();
    }

    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(formatter);
    }

    public LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, formatter);
    }
}