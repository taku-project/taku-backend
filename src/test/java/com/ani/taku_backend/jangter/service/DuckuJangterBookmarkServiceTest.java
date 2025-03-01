package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.common.enums.UserRole;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import com.ani.taku_backend.jangter.repository.DuckuJangterBookmarkRepository;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.jangter.vo.UserBookmarkHistory;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.model.entity.UserStatus;
import com.ani.taku_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.math.BigDecimal;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DuckuJangterBookmarkServiceTest {

    @Mock
    private DuckuJangterBookmarkRepository bookmarkRepository;

    @Mock
    private DuckuJangterRepository jangterRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DuckuJangterBookmarkServiceImpl bookmarkService;

    private User testUser;
    private DuckuJangter testJangter;
    private DuckuJangterBookmark testBookmark;
    private final Long userId = 1L;
    private final Long productId = 100L;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(userId)
                .nickname("테스트사용자")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        testJangter = DuckuJangter.builder()
                .id(productId)
                .title("테스트 상품")
                .build();

        testBookmark = DuckuJangterBookmark.create(testUser, testJangter);
    }

    @Test
    @DisplayName("북마크 추가 - 신규 북마크 생성")
    void addBookmark_NewBookmark_Success() {

        when(bookmarkRepository.findByUserUserIdAndJangterId(userId, productId))
                .thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(jangterRepository.findById(productId)).thenReturn(Optional.of(testJangter));


        bookmarkService.addBookmark(userId, productId);


        verify(bookmarkRepository).save(any(DuckuJangterBookmark.class));
        verify(bookmarkRepository, times(1)).findByUserUserIdAndJangterId(userId, productId);
    }

    @Test
    @DisplayName("북마크 추가 - 기존 비활성화된 북마크 재활성화")
    void addBookmark_ReactivateInactiveBookmark_Success() {

        testBookmark.deactivate();
        when(bookmarkRepository.findByUserUserIdAndJangterId(userId, productId))
                .thenReturn(Optional.of(testBookmark));

        bookmarkService.addBookmark(userId, productId);


        assertThat(testBookmark.getIsActive()).isTrue();
        verify(bookmarkRepository, never()).save(any(DuckuJangterBookmark.class));
    }

    @Test
    @DisplayName("북마크 추가 - 이미 활성화된 북마크가 존재하면 예외 발생")
    void addBookmark_AlreadyActiveBookmark_ThrowsException() {

        when(bookmarkRepository.findByUserUserIdAndJangterId(userId, productId))
                .thenReturn(Optional.of(testBookmark));


        assertThatThrownBy(() -> bookmarkService.addBookmark(userId, productId))
                .isInstanceOf(DuckwhoException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_BOOKMARKED);
    }

    @Test
    @DisplayName("북마크 제거 - 북마크 비활성화 성공")
    void removeBookmark_Success() {

        testBookmark.activate();

        when(bookmarkRepository.findByUserUserIdAndJangterId(userId, productId))
                .thenReturn(Optional.of(testBookmark));

        bookmarkService.removeBookmark(userId, productId);

        assertThat(testBookmark.getIsActive()).isFalse();

        verify(bookmarkRepository).findByUserUserIdAndJangterId(userId, productId);
    }

    @Test
    @DisplayName("사용자 북마크 이력 조회")
    void getUserBookmarkHistory_Success() {

        List<String> keywords = List.of("키워드1", "키워드2");


        UserBookmarkHistory mockHistory = UserBookmarkHistory.builder()
                .keywords(keywords)
                .categoryIds(List.of(1L))
                .avgPrice(new BigDecimal("10000"))
                .minPrice(new BigDecimal("10000"))
                .maxPrice(new BigDecimal("10000"))
                .build();


        DuckuJangterBookmarkService spyService = spy(bookmarkService);
        doReturn(mockHistory).when(spyService).getUserBookmarkHistory(userId, keywords);

        UserBookmarkHistory result = spyService.getUserBookmarkHistory(userId, keywords);


        assertThat(result).isNotNull();
        assertThat(result.getKeywords()).containsExactlyInAnyOrder("키워드1", "키워드2");
        assertThat(result.getCategoryIds()).hasSize(1);
    }
}