package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.jangter.model.dto.ItemCategoriesResponseDTO;
import com.ani.taku_backend.jangter.model.entity.ItemCategories;
import com.ani.taku_backend.jangter.repository.ItemCategoriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemCategoriesService {

    private final ItemCategoriesRepository itemCategoriesRepository;

    public ItemCategoriesResponseDTO findAllItemCategories() {

        List<ItemCategories> itemCategoriesAll = itemCategoriesRepository.findAll();

        return new ItemCategoriesResponseDTO(itemCategoriesAll);
    }
}
