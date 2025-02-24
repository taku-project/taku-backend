package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.bookmark.domain.Bookmark;
import com.ani.taku_backend.bookmark.service.BookmarkService;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.model.dto.BookmarkListResponseDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import com.ani.taku_backend.jangter.repository.DuckuJangterBookmarkRepository;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DuckuJangterBookmarkServiceTest {

    @InjectMocks
    private DuckuJangterBookmarkServiceImpl bookmarkService;

    @Mock
    private DuckuJangterBookmarkRepository bookmarkRepository;

    @Mock
    private DuckuJangterRepository jangterRepository;

    @Mock
    private BookmarkService userBookmarkService;

    @Test
    @DisplayName("북마크 목록을 페이징하여 조회할 수 있다")
    void getBookmarkList_Success() {
        Long userId = 1L;
        Long categoryId = 0L;  // 전체 카테고리 조회
        Pageable pageable = PageRequest.of(0, 20);
        
        List<BookmarkListResponseDTO> bookmarks = List.of(
            BookmarkListResponseDTO.builder()
                .productId(1L)
                .title("테스트 상품")
                .build()
        );
        Page<BookmarkListResponseDTO> expectedPage = new PageImpl<>(bookmarks, pageable, 1);
        
        // categoryId가 0일 때는 null로 처리됨
        given(bookmarkRepository.findBookmarksByUserIdWithPaging(eq(userId), isNull(), eq(pageable)))
                .willReturn(expectedPage);

        Page<BookmarkListResponseDTO> result = bookmarkService.getBookmarkList(userId, categoryId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 상품");
    }

    @Test
    @DisplayName("상품을 북마크에 추가할 수 있다")
    void addBookmark_Success() {
 
        Long userId = 1L;
        Long productId = 1L;
        DuckuJangter jangter = DuckuJangter.builder()
                .id(productId)
                .title("테스트 상품")
                .build();
        Bookmark userBookmark = Bookmark.builder()
                .id(1L)
                .build();

        given(bookmarkRepository.existsByBookmark_User_UserIdAndJangter_Id(userId, productId))
                .willReturn(false);
        given(jangterRepository.findById(productId))
                .willReturn(Optional.of(jangter));
        given(userBookmarkService.getBookmarkByUserId(userId))
                .willReturn(userBookmark);

        // when
        bookmarkService.addBookmark(userId, productId);

        // then
        verify(bookmarkRepository).save(any(DuckuJangterBookmark.class));
    }

    @Test
    @DisplayName("북마크를 삭제할 수 있다")
    void removeBookmark_Success() {
        // given
        Long userId = 1L;
        Long productId = 1L;
        DuckuJangterBookmark bookmark = DuckuJangterBookmark.builder()
                .id(1L)
                .build();

        given(bookmarkRepository.findByBookmark_User_UserIdAndJangter_Id(userId, productId))
                .willReturn(Optional.of(bookmark));

        // when
        bookmarkService.removeBookmark(userId, productId);

        // then
        verify(bookmarkRepository).delete(bookmark);
    }

    @Test
    @DisplayName("이미 북마크된 상품을 다시 북마크하면 예외가 발생한다")
    void addBookmark_AlreadyBookmarked() {
        // given
        Long userId = 1L;
        Long productId = 1L;
        given(bookmarkRepository.existsByBookmark_User_UserIdAndJangter_Id(userId, productId))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> bookmarkService.addBookmark(userId, productId))
                .isInstanceOf(DuckwhoException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_BOOKMARKED);
    }

    @Test
    @DisplayName("존재하지 않는 상품을 북마크하면 예외가 발생한다")
    void addBookmark_ProductNotFound() {
        // given
        Long userId = 1L;
        Long productId = 999L;
        given(bookmarkRepository.existsByBookmark_User_UserIdAndJangter_Id(userId, productId))
                .willReturn(false);
        given(jangterRepository.findById(productId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookmarkService.addBookmark(userId, productId))
                .isInstanceOf(DuckwhoException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("잘못된 카테고리로 북마크 목록을 조회하면 예외가 발생한다")
    void getBookmarkList_InvalidCategory() {
        // given
        Long userId = 1L;
        Long invalidCategoryId = -1L;
        Pageable pageable = PageRequest.of(0, 20);

        // when & then
        assertThatThrownBy(() -> bookmarkService.getBookmarkList(userId, invalidCategoryId, pageable))
                .isInstanceOf(DuckwhoException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode. NOT_FOUND_CATEGORY);
    }
} 