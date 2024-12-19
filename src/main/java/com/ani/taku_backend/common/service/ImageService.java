package com.ani.taku_backend.common.service;

import org.springframework.stereotype.Service;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.repository.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;


    public void insertImage(Image image){
        this.imageRepository.save(image);
    }
    
}
