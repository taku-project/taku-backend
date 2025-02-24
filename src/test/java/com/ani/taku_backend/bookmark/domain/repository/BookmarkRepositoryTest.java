package com.ani.taku_backend.bookmark.domain.repository;

import com.ani.taku_backend.bookmark.domain.Bookmark;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import com.ani.taku_backend.user.model.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookmarkRepositoryTest {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("사용자 ID로 북마크를 찾을 수 있다")
    void findByUser_UserId() {

        User user = createAndPersistUser();
        Bookmark bookmark = createAndPersistBookmark(user);


        Optional<Bookmark> found = bookmarkRepository.findByUser_UserId(user.getUserId());


        assertThat(found).isPresent();
        assertThat(found.get().getUser().getUserId()).isEqualTo(user.getUserId());
    }

    @Test
    @DisplayName("사용자의 장터 북마크 목록을 조회할 수 있다")
    void findByUserIdWithJangterAndCategories() {

        User user = createAndPersistUser();
        Bookmark bookmark = createAndPersistBookmark(user);
        DuckuJangter jangter1 = createAndPersistJangter("상품1");
        DuckuJangter jangter2 = createAndPersistJangter("상품2");
        createAndPersistJangterBookmark(bookmark, jangter1);
        createAndPersistJangterBookmark(bookmark, jangter2);


        List<Bookmark> bookmarks = bookmarkRepository.findByUserIdWithJangterAndCategories(user.getUserId());


        assertThat(bookmarks).hasSize(1);
        assertThat(bookmarks.get(0).getDuckuJangterBookmarks()).hasSize(2);
    }

    private User createAndPersistUser() {
        User user = User.builder()
                .email("test@example.com")
                .nickname("테스터")
                .build();
        return entityManager.persist(user);
    }

    private Bookmark createAndPersistBookmark(User user) {
        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .isActive(true)
                .build();
        return entityManager.persist(bookmark);
    }

    private DuckuJangter createAndPersistJangter(String title) {
        DuckuJangter jangter = DuckuJangter.builder()
                .title(title)
                .build();
        return entityManager.persist(jangter);
    }

    private DuckuJangterBookmark createAndPersistJangterBookmark(Bookmark bookmark, DuckuJangter jangter) {
        DuckuJangterBookmark jangterBookmark = DuckuJangterBookmark.builder()
                .bookmark(bookmark)
                .jangter(jangter)
                .build();
        return entityManager.persist(jangterBookmark);
    }
} 