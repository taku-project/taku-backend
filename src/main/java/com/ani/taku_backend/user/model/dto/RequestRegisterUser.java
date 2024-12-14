package com.ani.taku_backend.user.model.dto;


import com.ani.taku_backend.common.enums.ProviderType;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestRegisterUser {

  @JsonProperty("nickname")
  private String nickname;

  @JsonProperty("provider_type")
  private ProviderType providerType;
}
