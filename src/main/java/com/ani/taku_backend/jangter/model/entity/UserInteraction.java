package com.ani.taku_backend.jangter.model.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.ani.taku_backend.common.enums.LogType;
import com.ani.taku_backend.common.enums.SortFilterType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Document(collection = "user_interactions")
@ToString
public class UserInteraction<T extends UserInteraction.LogDetail> {
    @Id
    private ObjectId id;
    
    @Field("user_id")
    private Long userId;

    @Field("interaction_type")
    private LogType logType;

    @Field("log_detail")
    private T logDetail;

    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    public interface LogDetail {}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class SearchLogDetail implements LogDetail {
        @Field("search_keyword")
        private String searchKeyword;

        @Field("search_category")
        private List<Long> searchCategory;

        @Field("sort_type")
        private SortFilterType sortType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class ViewLogDetail implements LogDetail {
        @Field("product_id")
        private Long productId;
    }
}



