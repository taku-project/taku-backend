package com.ani.taku_backend.example.model.entity;

import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.example.model.dto.ExampleDetailResponse;
import com.ani.taku_backend.example.model.dto.ExampleUpdateRequest;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Example extends BaseTimeEntity {
    @Id
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID exampleId = UUID.randomUUID();

    @NotNull
    @Column(length = 50, nullable = false)
    private String exampleTitle;

    @NotNull
    @Column(length = 255, nullable = false)
    private String exampleContent;

    @Builder
    public Example(String exampleTitle, String exampleContent) {
        this.exampleTitle = exampleTitle;
        this.exampleContent = exampleContent;
    }

    public void updateExample(ExampleUpdateRequest request) {
        this.exampleTitle = request.getExampleTitle();
        this.exampleContent = request.getExampleContent();
    }

    public ExampleDetailResponse toDetailResponse() {
        return new ExampleDetailResponse(
                this.exampleId,
                this.exampleTitle,
                this.exampleContent
        );
    }
}
