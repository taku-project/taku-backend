package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.common.enums.UserRole;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.model.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuckuJangterBookmarkRepositoryTest {

    @Mock
    private DuckuJangterBookmarkRepository bookmarkRepository;

    @Test
    @DisplayName("사용자 ID와 상품 ID로 북마크 조회")
    void findByUserUserIdAndJangterId() {

        User user = createTestUser(1L);
        DuckuJangter jangter = createTestJangter(100L);
        DuckuJangterBookmark bookmark = DuckuJangterBookmark.create(user, jangter);

        when(bookmarkRepository.findByUserUserIdAndJangterId(1L, 100L))
                .thenReturn(Optional.of(bookmark));


        Optional<DuckuJangterBookmark> found = bookmarkRepository.findByUserUserIdAndJangterId(1L, 100L);


        assertThat(found).isPresent();
        assertThat(found.get().getUser().getUserId()).isEqualTo(1L);
        assertThat(found.get().getJangter().getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("사용자 ID로 모든 활성화된 북마크 목록 조회")
    void findByUserUserIdAndIsActiveTrue() {

        User user = createTestUser(1L);
        DuckuJangter jangter = createTestJangter(100L);
        DuckuJangterBookmark bookmark = DuckuJangterBookmark.create(user, jangter);
        bookmark.activate();

        when(bookmarkRepository.findByUserUserIdAndIsActiveTrue(1L))
                .thenReturn(List.of(bookmark));


        List<DuckuJangterBookmark> bookmarks = bookmarkRepository.findByUserUserIdAndIsActiveTrue(1L);


        assertThat(bookmarks).hasSize(1);
        assertThat(bookmarks.get(0).getJangter().getId()).isEqualTo(100L);
    }


    private User createTestUser(Long id) {
        return User.builder()
                .userId(id)
                .nickname("테스트 사용자")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();
    }

    private DuckuJangter createTestJangter(Long id) {
        return DuckuJangter.builder()
                .id(id)
                .title("테스트 상품")
                .build();
    }
}