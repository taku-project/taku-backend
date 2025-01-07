package com.ani.taku_backend.user.model.dto;


import com.ani.taku_backend.common.enums.ProviderType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "유저 등록 요청 정보")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestRegisterUser {

  @JsonProperty("nickname")
  @Schema(description = "유저 닉네임", example = "looco")
  private String nickname;

  @JsonProperty("provider_type")
  @Schema(description = "유저 프로바이더 타입", example = "{kakao , google}")
  private ProviderType providerType;
}
