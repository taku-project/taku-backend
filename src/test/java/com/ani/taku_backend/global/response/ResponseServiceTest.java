package com.ani.taku_backend.global.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ResponseServiceTest {

    @Autowired
    private ResponseService responseService;

    @Test
    @DisplayName("단일 데이터 응답을 SUCCESS 형식으로 반환하는지 테스트")
    void testGetSingleResponse() {
        String testData = "Hello World";
        SingleResponse<String> singleResponse = responseService.getSingleResponse(testData);

        assertNotNull(singleResponse);
        assertTrue(singleResponse.isSuccess());
        assertEquals(0, singleResponse.getCode());
        assertEquals("SUCCESS", singleResponse.getMessage());
        assertEquals(testData, singleResponse.getData());
    }

    @Test
    @DisplayName("리스트 데이터 응답을 SUCCESS 형식으로 반환하는지 테스트")
    void testGetListResponse() {
        List<String> testDataList = Arrays.asList("Item1", "Item2", "Item3");
        ListResponse<String> listResponse = responseService.getListResponse(testDataList);

        assertNotNull(listResponse);
        assertTrue(listResponse.isSuccess());
        assertEquals(0, listResponse.getCode());
        assertEquals("SUCCESS", listResponse.getMessage());
        assertEquals(testDataList, listResponse.getDataList());
    }

    @Test
    @DisplayName("에러 응답을 코드와 메시지로 반환하는지 테스트")
    void testGetErrorResponse() {
        int errorCode = 1000;
        String errorMessage = "해당하는 사용자가 없습니다.";
        CommonResponse errorResponse = responseService.getErrorResponse(errorCode, errorMessage);

        assertNotNull(errorResponse);
        assertFalse(errorResponse.isSuccess());
        assertEquals(errorCode, errorResponse.getCode());
        assertEquals(errorMessage, errorResponse.getMessage());
    }

    @Test
    @DisplayName("null 데이터를 단일 응답 형태로 반환 시 SUCCESS 형식 유지 확인")
    void testGetSingleResponseWithNullData() {
        SingleResponse<String> singleResponse = responseService.getSingleResponse(null);

        assertNotNull(singleResponse);
        assertTrue(singleResponse.isSuccess());
        assertEquals(0, singleResponse.getCode());
        assertEquals("SUCCESS", singleResponse.getMessage());
        assertNull(singleResponse.getData());
    }

    @Test
    @DisplayName("빈 리스트 데이터를 리스트 응답 형태로 반환 시 SUCCESS 형식 유지 확인")
    void testGetListResponseWithEmptyList() {
        ListResponse<String> listResponse = responseService.getListResponse(List.of());

        assertNotNull(listResponse);
        assertTrue(listResponse.isSuccess());
        assertEquals(0, listResponse.getCode());
        assertEquals("SUCCESS", listResponse.getMessage());
        assertTrue(listResponse.getDataList().isEmpty());
    }
}