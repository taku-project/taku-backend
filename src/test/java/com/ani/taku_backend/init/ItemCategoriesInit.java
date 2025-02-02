package com.ani.taku_backend.init;

import com.ani.taku_backend.jangter.model.entity.ItemCategories;
import com.ani.taku_backend.jangter.repository.ItemCategoriesRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ItemCategoriesInit {

    @Autowired
    ItemCategoriesRepository itemCategoriesRepository;

    @Test
    void init() {
        ItemCategories[] itemCategoryList = {
                new ItemCategories(null, "디지털기기"),
                new ItemCategories(null, "가구/인테리어"),
                new ItemCategories(null, "여성의류"),
                new ItemCategories(null, "여성잡화"),
                new ItemCategories(null, "남성패션/잡화"),
                new ItemCategories(null, "생활가전"),
                new ItemCategories(null, "생활/주방"),
                new ItemCategories(null, "취미/게임/음반"),
                new ItemCategories(null, "뷰티/미용"),
                new ItemCategories(null, "도서"),
                new ItemCategories(null, "기타 중고물품")};

        for (ItemCategories itemCategories : itemCategoryList) {
            itemCategoriesRepository.save(itemCategories);
        }
    }
}
