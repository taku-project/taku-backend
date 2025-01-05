package com.ani.taku_backend.shorts.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VideoType {
    MP4("mp4", "MPEG-4"),
    WEBM("webm", "WebM"),
    TS("ts", "MPEG Transport Stream"),
    FLV("flv", "Flash Video"),
    MKV("mkv", "Matroska"),
    AVI("avi", "Audio Video Interleave"),
    MOV("mov", "QuickTime File Format"),
    WMV("wmv", "Windows Media Video"),
    MPEG("mpeg", "MPEG Video"),
    OGV("ogv", "Ogg Video");

    private final String extension;
    private final String description;

    public static VideoType fromExtension(String extension) {
        for (VideoType format : values()) {
            if (format.extension.equalsIgnoreCase(extension)) {
                return format;
            }
        }
        return null;
    }

    public static boolean isSupportedFileFormat(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return false; // No extension found
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return fromExtension(extension) != null;
    }
}
