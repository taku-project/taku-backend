package com.ani.taku_backend.bookmark.controller;

import com.ani.taku_backend.bookmark.domain.dto.DuckuJangterBookmarkResponseDTO;
import com.ani.taku_backend.bookmark.service.DuckuJangterBookmarkService;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DuckuJangterBookmarkController.class)
class DuckuJangterBookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DuckuJangterBookmarkService bookmarkService;

    @Test
    @WithMockUser
    @DisplayName("북마크 추가 성공")
    void addBookmark_Success() throws Exception {
        // given
        Long userId = 1L;
        Long productId = 1L;

        // when & then
        mockMvc.perform(post("/api/jangter/bookmarks/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("이미 북마크된 상품 추가 시 409 응답")
    void addBookmark_AlreadyBookmarked_Returns409() throws Exception {
        // given
        Long userId = 1L;
        Long productId = 1L;
        doThrow(new DuckwhoException(ErrorCode.ALREADY_BOOKMARKED))
                .when(bookmarkService).addBookmark(any(), eq(productId));

        // when & then
        mockMvc.perform(post("/api/jangter/bookmarks/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    @DisplayName("북마크 삭제 성공")
    void removeBookmark_Success() throws Exception {
        // given
        Long userId = 1L;
        Long productId = 1L;

        // when & then
        mockMvc.perform(delete("/api/jangter/bookmarks/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("카테고리별 북마크 조회 성공")
    void getBookmarksByCategory_Success() throws Exception {
        // given
        Long userId = 1L;
        Long categoryId = 1L;
        DuckuJangterBookmarkResponseDTO dto = DuckuJangterBookmarkResponseDTO.builder()
                .id(1L)
                .title("테스트 상품")
                .price(new BigDecimal("10000"))
                .category("피규어")
                .imageUrl("test.jpg")
                .createdAt(LocalDateTime.now())
                .build();
        Page<DuckuJangterBookmarkResponseDTO> page = new PageImpl<>(List.of(dto));

        given(bookmarkService.getBookmarksByCategory(any(), eq(categoryId), any()))
                .willReturn(page);

        // when & then
        mockMvc.perform(get("/api/jangter/bookmarks/category/{categoryId}", categoryId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("존재하지 않는 카테고리로 조회 시 404 응답")
    void getBookmarksByCategory_NotFoundCategory_Returns404() throws Exception {
        // given
        Long userId = 1L;
        Long categoryId = 999L;

        doThrow(new DuckwhoException(ErrorCode.NOT_FOUND_CATEGORY))
                .when(bookmarkService).getBookmarksByCategory(any(), eq(categoryId), any());

        // when & then
        mockMvc.perform(get("/api/jangter/bookmarks/category/{categoryId}", categoryId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
