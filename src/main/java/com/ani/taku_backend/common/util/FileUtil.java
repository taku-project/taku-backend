package com.ani.taku_backend.common.util;

import java.util.Arrays;
import java.util.UUID;

public class FileUtil {

    // img 확장자 체크 상수
    public static final String[] IMG_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp", "tiff", "ico", "webp"};
    
    // 확장자
    public static String getExtension(String fileName){
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    // 이미지 확장자 체크
    public static boolean isImgExtension(String fileName){
        String extension = getExtension(fileName);
        return Arrays.asList(IMG_EXTENSIONS).contains(extension);
    }

    // uuid 파일명 생성
    public static String getUuidFileName(String fileName){
        return UUID.randomUUID().toString() + "." + getExtension(fileName);
    }
}
