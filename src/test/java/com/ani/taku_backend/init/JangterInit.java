package com.ani.taku_backend.init;

import com.ani.taku_backend.jangter.model.dto.ProductCreateRequestDTO;
import com.ani.taku_backend.jangter.model.entity.ItemCategories;
import com.ani.taku_backend.jangter.repository.ItemCategoriesRepository;
import com.ani.taku_backend.jangter.service.DuckuJangterService;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class JangterInit {

    private static final Logger logger = LoggerFactory.getLogger(JangterInit.class);


    @Autowired
    DuckuJangterService duckuJangterService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemCategoriesRepository itemCategoriesRepository;

    @Test
    void init() throws InterruptedException {
        int minPrice = 1000;
        int maxPrice = 100000;
        int batchSize = 500;
        int totalJangters = 10000;
        int threadCount = 4;

        List<User> allUser = userRepository.findAll();
        List<ItemCategories> allItemCategories = itemCategoriesRepository.findAll();
        List<MultipartFile> imageList = loadJpgImages("/Users/jinagyeomi/Downloads/jangter/dummy");

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        for (int start = 0; start < totalJangters; start += batchSize * threadCount) {
            for (int thread = 0; thread < threadCount; thread++) {
                executorService.submit(() -> {

                    List<MultipartFile> threadImageList = imageList != null ?
                            new ArrayList<>(imageList) :
                            new ArrayList<>();

                    List<ProductCreateRequestDTO> batchJangters = new ArrayList<>();

                    for (int i = 1; i <= batchSize; i++) {

                        ItemCategories itemCategories = allItemCategories.get(ThreadLocalRandom.current().nextInt(allItemCategories.size()));

                        int price = (ThreadLocalRandom.current().nextInt((maxPrice - minPrice) / 1000 + 1) * 1000) + minPrice;
                        BigDecimal bigDecimal = BigDecimal.valueOf(price);

                        List<MultipartFile> resultImages = getMultipartFiles(threadImageList, ThreadLocalRandom.current());

                        ProductCreateRequestDTO createDTO = new ProductCreateRequestDTO();
                        createDTO.setCategoryId(itemCategories.getId());
                        createDTO.setTitle(itemCategories.getName() + i);
                        createDTO.setDescription(itemCategories.getName() + i + " 판매합니다~~");
                        createDTO.setPrice(bigDecimal);
                        createDTO.setImageList(resultImages);

                        batchJangters.add(createDTO);
                    }

                    for (ProductCreateRequestDTO requestDTO : batchJangters) {
                        User user = allUser.get(ThreadLocalRandom.current().nextInt(allUser.size()));
                        duckuJangterService.createProduct(requestDTO, user);
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

    // 랜덤 이미지 선택
    public static List<MultipartFile> getMultipartFiles(List<MultipartFile> imageList, ThreadLocalRandom random) {
        int maxImages = 5;
        List<MultipartFile> resultImages = null;
        if ((imageList != null) && !imageList.isEmpty()) {
            int randomImageCount = random.nextInt(Math.min(maxImages, imageList.size() + 1));
            Collections.shuffle(imageList, random);
            resultImages = imageList.subList(0, randomImageCount);
        }
        return resultImages;
    }

    public static List<MultipartFile> loadJpgImages(String directoryPath) {
        List<MultipartFile> imageList = new ArrayList<>();
        File directory = new File(directoryPath);

        if (!directory.isDirectory()) {
            return null;
        }

        File[] imageFiles = directory.listFiles((dir, name) -> {
            String lowerCaseName = name.toLowerCase();
            return lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".png") || lowerCaseName.endsWith(".gif") || lowerCaseName.endsWith(".jpeg");
        });

        if (imageFiles == null || imageFiles.length == 0) {
            return null;
        }

        for (File imageFile : imageFiles) {
            try (FileInputStream inputStream = new FileInputStream(imageFile)) {
                MultipartFile multipartFile = new MockMultipartFile(
                        imageFile.getName(),
                        imageFile.getName(),
                        "image/" + getExtension(imageFile.getName()),
                        inputStream);
                imageList.add(multipartFile);
            } catch (IOException e) {
                return null;
            }
        }
        return imageList;
    }

    public static String getExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf(".");
        return (lastIndex != -1) ? fileName.substring(lastIndex + 1).toLowerCase() : "";
    }
}
