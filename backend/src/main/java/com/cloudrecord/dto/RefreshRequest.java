package com.cloudrecord.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequest {

    @NotBlank(message = "refresh_token 不能为空")
    private String refreshToken;
}
