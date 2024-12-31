package com.ani.taku_backend.shorts.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortsFFmPegUrlResDTO {
    @SerializedName("message")
    private String message;
    @SerializedName("m3u8_url")
    private String m3u8Url;
    @SerializedName("segments")
    private List<String> segments;
    @SerializedName("duration")
    private double duration;
}
