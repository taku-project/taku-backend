package com.ani.taku_backend.common.remote_file;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoteFileServiceFactory {
    private final CloudflareR2ImageFileService imageFileService;
    private final CloudflareR2VideoFileService videoFileService;

    public RemoteFileService getService(String fileType) {
        if (fileType.startsWith("image/")) {
            return imageFileService;
        } else if (fileType.startsWith("video/")) {
            return videoFileService;
        }
        throw new DuckwhoException(ErrorCode.INVALID_FILE_FORMAT);
    }
}
