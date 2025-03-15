package com.ani.taku_backend.init;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.repository.CategoryRepository;
import com.ani.taku_backend.post.model.dto.PostCreateRequestDTO;
import com.ani.taku_backend.post.service.PostService;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.ani.taku_backend.init.JangterInit.getMultipartFiles;
import static com.ani.taku_backend.init.JangterInit.loadJpgImages;

@SpringBootTest
public class PostInit {

    private static final Logger logger = LoggerFactory.getLogger(PostInit.class);

    @Autowired
    private PostService postService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryRepository categoryRepository;

//    @Test
    void init() throws InterruptedException {

        int batchSize = 500;
        int totalPosts = 1000;
        int threadCount = 4;

        List<User> allUser = userRepository.findAll();
        List<Category> allCategory = categoryRepository.findAll();
        List<MultipartFile> imageList = loadJpgImages("/Users/jinagyeomi/Downloads/postImage/dummy");

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        for (int start = 0; start < totalPosts; start += batchSize * threadCount) {
            for (int thread = 0; thread < threadCount; thread++) {
                int batchStartIndex = start + (thread * batchSize);
                logger.info("Thread 시작: Batch Index = {}", batchStartIndex);
                executorService.submit(() -> {

                    List<MultipartFile> threadImageList = imageList != null ?
                            new ArrayList<>(imageList) :
                            null;

                    List<PostCreateRequestDTO> batchPosts = new ArrayList<>();
                    for (int i = 1; i <= batchSize; i++) {
                        if (batchStartIndex + i >= totalPosts) {
                            break;
                        }

                        Category category = allCategory.get(ThreadLocalRandom.current().nextInt(allCategory.size()));
                        List<MultipartFile> resultImages = getMultipartFiles(threadImageList, ThreadLocalRandom.current());

                        PostCreateRequestDTO requestDTO = new PostCreateRequestDTO();
                        requestDTO.setCategoryId(category.getId());
                        requestDTO.setTitle(category.getName() + "에 대해서 어떻게 생각해요?" + i);
                        requestDTO.setContent(category.getName() + " 에 대한 내용 입니다" + i);
                        requestDTO.setImageList(resultImages);

                        batchPosts.add(requestDTO);
                    }
                    logger.info("Batch 생성 완료: Batch Index = {}, Batch Size = {}", batchStartIndex, batchPosts.size());

                    for (PostCreateRequestDTO requestDTO : batchPosts) {
                        User randomUser = allUser.get(ThreadLocalRandom.current().nextInt(allUser.size()));
                        postService.createPost(requestDTO, randomUser);
                        logger.info("Post 생성 완료: Title = {}", requestDTO.getTitle());
                    }
                });
            }
        }

        logger.info("전체 배치 작업 시작");
        executorService.shutdown();
        if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
            logger.error("작업이 시간 내에 종료되지 않았습니다.");
        } else {
            logger.info("전체 배치 작업 종료");
        }
    }

}
