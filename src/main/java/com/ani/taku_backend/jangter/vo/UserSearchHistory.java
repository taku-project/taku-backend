package com.ani.taku_backend.jangter.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class UserSearchHistory {

    private List<String> keywords;

    private List<Long> categoryIds;

    public static UserSearchHistory create(List<String> keywords, List<Long> categoryIds){
        return UserSearchHistory.builder()
            .keywords(keywords)
            .categoryIds(categoryIds)
            .build();
    }
}