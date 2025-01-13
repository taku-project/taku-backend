package com.ani.taku_backend.marketprice.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateTimeUtil {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");

    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }

    public LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, FORMATTER);
    }
}