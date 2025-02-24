package com.ani.taku_backend.bookmark.service;

import com.ani.taku_backend.bookmark.domain.Bookmark;
import com.ani.taku_backend.bookmark.domain.repository.BookmarkRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @InjectMocks
    private BookmarkServiceImpl bookmarkService;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자의 북마크 목록을 조회할 수 있다")
    void findByUserIdWithJangterAndCategories() {

        Long userId = 1L;
        List<Bookmark> expectedBookmarks = List.of(
                Bookmark.builder().id(1L).build(),
                Bookmark.builder().id(2L).build()
        );
        given(bookmarkRepository.findByUserIdWithJangterAndCategories(userId))
                .willReturn(expectedBookmarks);


        List<Bookmark> bookmarks = bookmarkService.findByUserIdWithJangterAndCategories(userId);


        assertThat(bookmarks).hasSize(2);
        verify(bookmarkRepository).findByUserIdWithJangterAndCategories(userId);
    }

    @Test
    @DisplayName("존재하는 사용자의 북마크를 조회할 수 있다")
    void getBookmarkByUserId_ExistingBookmark() {

        Long userId = 1L;
        Bookmark expectedBookmark = Bookmark.builder().id(1L).build();
        given(bookmarkRepository.findByUser_UserId(userId))
                .willReturn(Optional.of(expectedBookmark));


        Bookmark bookmark = bookmarkService.getBookmarkByUserId(userId);

        assertThat(bookmark).isNotNull();
        assertThat(bookmark.getId()).isEqualTo(expectedBookmark.getId());
    }

    @Test
    @DisplayName("북마크가 없는 사용자의 경우 새로운 북마크를 생성한다")
    void getBookmarkByUserId_CreateNewBookmark() {

        Long userId = 1L;
        User user = User.builder().userId(userId).build();
        Bookmark newBookmark = Bookmark.builder().id(1L).user(user).build();

        given(bookmarkRepository.findByUser_UserId(userId)).willReturn(Optional.empty());
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(bookmarkRepository.save(any(Bookmark.class))).willReturn(newBookmark);


        Bookmark bookmark = bookmarkService.getBookmarkByUserId(userId);


        assertThat(bookmark).isNotNull();
        assertThat(bookmark.getUser()).isEqualTo(user);
        verify(bookmarkRepository).save(any(Bookmark.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 경우 예외가 발생한다")
    void getBookmarkByUserId_UserNotFound() {

        Long userId = 1L;
        given(bookmarkRepository.findByUser_UserId(userId)).willReturn(Optional.empty());
        given(userRepository.findById(userId)).willReturn(Optional.empty());


        assertThatThrownBy(() -> bookmarkService.getBookmarkByUserId(userId))
                .isInstanceOf(DuckwhoException.class);
    }
} 