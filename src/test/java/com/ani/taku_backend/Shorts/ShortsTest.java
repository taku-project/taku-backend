package com.ani.taku_backend.Shorts;

import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.PostRepository;
import com.ani.taku_backend.shorts.domain.entity.Shorts;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.AccessibleObject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootTest
class ShortsTest {



//    @Test
    @Transactional
    @Commit
    void saveTestData() {
        AccessibleObject accessibleObject = null;
    }

    @Test
    void noOffsetPagingTest() {

    }

}