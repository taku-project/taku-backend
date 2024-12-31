package com.ani.taku_backend.example.controller;

import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.example.model.dto.ExampleCreateRequest;
import com.ani.taku_backend.example.model.dto.ExampleDetailResponse;
import com.ani.taku_backend.example.model.dto.ExampleUpdateRequest;
import com.ani.taku_backend.example.service.ExampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/example")
@Tag(name = "예시 API", description = "예시를 보여주기 위한 API")
public class ExampleController {

    private final ExampleService exampleService;

    @Operation(summary = "예시 생성")
    @PostMapping
    public CommonResponse<String> createExample(@Valid @RequestBody ExampleCreateRequest request) {
        String exampleId = exampleService.createExample(request);
        return CommonResponse.created("예제 생성완료: " + exampleId);
    }

    @Operation(summary = "예시 단건 조회")
    @GetMapping("/{id}")
    public CommonResponse<ExampleDetailResponse> findExampleById(
            @Schema(example = "042482cb-f1cd-4935-9579-e12da625961f")
            @PathVariable UUID id) {
        return CommonResponse.ok(exampleService.findExampleById(id));
    }

    @Operation(summary = "예시 다건 조회")
    @GetMapping
    public CommonResponse<List<ExampleDetailResponse>> findExampleList() {
        return CommonResponse.ok(exampleService.findExampleList());
    }

    @Operation(summary = "QueryDSL 예시 조회")
    @GetMapping("/querydsl/title")
    public CommonResponse<List<ExampleDetailResponse>> findExampleByTitle(
            @Parameter(required = true) String exampleTitle) {
        return CommonResponse.ok(exampleService.findExampleByTitle(exampleTitle));
    }

    @Operation(summary = "예시 단건 수정")
    @PatchMapping
    public CommonResponse<String> updateExample(@Valid @RequestBody ExampleUpdateRequest request) {
        String exampleId = exampleService.updateExample(request);
        return CommonResponse.ok("Example updated : " + exampleId);
    }
}