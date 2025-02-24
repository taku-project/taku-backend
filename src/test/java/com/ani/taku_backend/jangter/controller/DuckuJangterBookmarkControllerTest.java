package com.ani.taku_backend.jangter.controller;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.jangter.model.dto.BookmarkListResponseDTO;
import com.ani.taku_backend.jangter.service.DuckuJangterBookmarkService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DuckuJangterBookmarkController.class)
class DuckuJangterBookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DuckuJangterBookmarkService bookmarkService;

    @Test
    @DisplayName("장터 상품을 북마크에 추가할 수 있다")
    @WithMockUser
    void addBookmark() throws Exception {

        Long productId = 1L;


        mockMvc.perform(post("/api/bookmarks/jangter/{productId}", productId)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(bookmarkService).addBookmark(any(), eq(productId));
    }

    @Test
    @DisplayName("장터 상품을 북마크에서 삭제할 수 있다")
    @WithMockUser
    void removeBookmark() throws Exception {

        Long productId = 1L;


        mockMvc.perform(delete("/api/bookmarks/jangter/{productId}", productId)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(bookmarkService).removeBookmark(any(), eq(productId));
    }

    @Test
    @DisplayName("북마크 목록을 페이징하여 조회할 수 있다")
    @WithMockUser
    void getBookmarkList() throws Exception {

        Long categoryId = 0L;
        Page<BookmarkListResponseDTO> bookmarkPage = new PageImpl<>(
                List.of(BookmarkListResponseDTO.builder().productId(1L).title("테스트상품").build()),
                PageRequest.of(0, 20),
                1
        );

        given(bookmarkService.getBookmarkList(any(), eq(categoryId), any()))
                .willReturn(bookmarkPage);


        mockMvc.perform(get("/api/bookmarks/jangter")
                        .param("categoryId", String.valueOf(categoryId))
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].productId").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("테스트상품"));
    }

    @Test
    @DisplayName("존재하지 않는 상품을 북마크에 추가할 수 없다")
    @WithMockUser
    void addBookmark_ProductNotFound() throws Exception {

        Long productId = 999L;
        doThrow(new DuckwhoException(ErrorCode.PRODUCT_NOT_FOUND))
                .when(bookmarkService).addBookmark(any(), eq(productId));


        mockMvc.perform(post("/api/bookmarks/jangter/{productId}", productId)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 상품입니다."));
    }

    @Test
    @DisplayName("이미 북마크된 상품은 다시 북마크할 수 없다")
    @WithMockUser
    void addBookmark_AlreadyBookmarked() throws Exception {
        // given
        Long productId = 1L;
        doThrow(new DuckwhoException(ErrorCode.ALREADY_BOOKMARKED))
                .when(bookmarkService).addBookmark(any(), eq(productId));

        // when & then
        mockMvc.perform(post("/api/bookmarks/jangter/{productId}", productId)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 북마크된 상품입니다."));
    }

    @Test
    @DisplayName("존재하지 않는 북마크는 삭제할 수 없다")
    @WithMockUser
    void removeBookmark_NotFound() throws Exception {

        Long productId = 999L;
        doThrow(new DuckwhoException(ErrorCode.NOT_FOUND_CATEGORY_BOOKMARK))
                .when(bookmarkService).removeBookmark(any(), eq(productId));


        mockMvc.perform(delete("/api/bookmarks/jangter/{productId}", productId)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 북마크입니다."));
    }

    @Test
    @DisplayName("잘못된 카테고리 ID로 북마크 목록을 조회할 수 없다")
    @WithMockUser
    void getBookmarkList_InvalidCategory() throws Exception {

        Long invalidCategoryId = -1L;
        doThrow(new DuckwhoException(ErrorCode. NOT_FOUND_CATEGORY))
                .when(bookmarkService).getBookmarkList(any(), eq(invalidCategoryId), any());


        mockMvc.perform(get("/api/bookmarks/jangter")
                        .param("categoryId", String.valueOf(invalidCategoryId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 카테고리입니다."));
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 북마크를 추가할 수 없다")
    void addBookmark_Unauthorized() throws Exception {

        Long productId = 1L;


        mockMvc.perform(post("/api/bookmarks/jangter/{productId}", productId))
                .andExpect(status().isUnauthorized());
    }
} 