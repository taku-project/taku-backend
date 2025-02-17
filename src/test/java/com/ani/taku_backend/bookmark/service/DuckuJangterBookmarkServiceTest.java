//package com.ani.taku_backend.bookmark.service;
//
//import com.ani.taku_backend.bookmark.domain.Bookmark;
//import com.ani.taku_backend.bookmark.domain.repository.BookmarkRepository;
//import com.ani.taku_backend.bookmark.domain.repository.DuckuJangterBookmarkRepository;
//import com.ani.taku_backend.common.exception.DuckwhoException;
//import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
//import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
//import com.ani.taku_backend.jangter.model.entity.ItemCategories;
//import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
//import com.ani.taku_backend.jangter.repository.ItemCategoriesRepository;
//import com.ani.taku_backend.user.model.entity.User;
//import com.ani.taku_backend.user.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.verify;
//
//@ExtendWith(MockitoExtension.class)
//class DuckuJangterBookmarkServiceTest {
//
//    @InjectMocks
//    private DuckuJangterBookmarkServiceImpl bookmarkService;
//
//    @Mock
//    private BookmarkRepository bookmarkRepository;
//    @Mock
//    private DuckuJangterBookmarkRepository duckuJangterBookmarkRepository;
//    @Mock
//    private UserRepository userRepository;
//    @Mock
//    private DuckuJangterRepository duckuJangterRepository;
//    @Mock
//    private ItemCategoriesRepository itemCategoriesRepository;
//
//    private User user;
//    private DuckuJangter product;
//    private Bookmark bookmark;
//    private ItemCategories category;
//    private DuckuJangterBookmark duckuJangterBookmark;
//
//    @BeforeEach
//    void setUp() {
//        user = User.builder()
//                .userId(1L)
//                .build();
//
//        category = ItemCategories.builder()
//                .id(1L)
//                .name("피규어")
//                .build();
//
//        product = DuckuJangter.builder()
//                .id(1L)
//                .itemCategories(category)
//                .build();
//
//        bookmark = Bookmark.builder()
//                .id(1L)
//                .user(user)
//                .isActive(true)
//                .build();
//
//        duckuJangterBookmark = DuckuJangterBookmark.builder()
//                .id(1L)
//                .bookmark(bookmark)
//                .jangter(product)
//                .build();
//    }
//
//    @Test
//    @DisplayName("북마크 추가 성공")
//    void addBookmark_Success() {
//        // given
//        given(userRepository.findById(user.getUserId()).willReturn(Optional.of(user));
//        given(duckuJangterRepository.findById(product.getId())).willReturn(Optional.of(product));
//        given(bookmarkRepository.findActiveBookmarkByUserId(user.getUserId())).willReturn(Optional.of(bookmark));
//        given(duckuJangterBookmarkRepository.findByBookmarkAndJangter(bookmark, product)).willReturn(Optional.empty());
//        given(duckuJangterBookmarkRepository.save(any())).willReturn(duckuJangterBookmark);
//
//        // when
//        bookmarkService.addBookmark(user.getUserId(), product.getId());
//
//        // then
//        verify(duckuJangterBookmarkRepository).save(any());
//    }
//
//    @Test
//    @DisplayName("이미 북마크된 상품 추가 시 예외 발생")
//    void addBookmark_AlreadyBookmarked_ThrowsException() {
//        // given
//        given(userRepository.findById(user.getUserId())).willReturn(Optional.of(user));
//        given(duckuJangterRepository.findById(product.getId())).willReturn(Optional.of(product));
//        given(bookmarkRepository.findActiveBookmarkByUserId(user.getUserId()).willReturn(Optional.of(bookmark));
//        given(duckuJangterBookmarkRepository.findByBookmarkAndJangter(bookmark, product))
//                .willReturn(Optional.of(duckuJangterBookmark));
//
//        // when & then
//        assertThatThrownBy(() -> bookmarkService.addBookmark(user.getUserId(), product.getId()))
//                .isInstanceOf(DuckwhoException.class);
//    }
//
//    @Test
//    @DisplayName("북마크 삭제 성공")
//    void removeBookmark_Success() {
//        // given
//        given(userRepository.findById(user.getUserId()).willReturn(Optional.of(user));
//        given(duckuJangterRepository.findById(product.getId())).willReturn(Optional.of(product));
//        given(bookmarkRepository.findActiveBookmarkByUserId(user.getUserId()).willReturn(Optional.of(bookmark));
//
//        // when
//        bookmarkService.removeBookmark(user.getUserId(), product.getId());
//
//        // then
//        verify(duckuJangterBookmarkRepository).deleteByBookmarkAndJangter(bookmark, product);
//    }
//
//    @Test
//    @DisplayName("카테고리별 북마크 조회 성공")
//    void getBookmarksByCategory_Success() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<DuckuJangterBookmark> bookmarkPage = new PageImpl<>(List.of(duckuJangterBookmark));
//
//        given(bookmarkRepository.findActiveBookmarkByUserId(user.getUserId())).willReturn(Optional.of(bookmark));
//        given(itemCategoriesRepository.findById(category.getId())).willReturn(Optional.of(category));
//        given(duckuJangterBookmarkRepository.findAllByBookmarkAndCategoryWithJangter(bookmark, category.getId(), pageable))
//                .willReturn(bookmarkPage);
//
//        // when
//        var result = bookmarkService.getBookmarksByCategory(user.getUserId(), category.getId(), pageable);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.getContent()).hasSize(1);
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 카테고리로 조회 시 예외 발생")
//    void getBookmarksByCategory_NotFoundCategory_ThrowsException() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//        given(bookmarkRepository.findActiveBookmarkByUserId(user.getUserId()).willReturn(Optional.of(bookmark));
//        given(itemCategoriesRepository.findById(category.getId())).willReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() ->
//            bookmarkService.getBookmarksByCategory(user.getUserId(), category.getId(), pageable))
//                .isInstanceOf(DuckwhoException.class);
//    }
//}