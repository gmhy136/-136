package com.cloudrecord.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 64, message = "昵称最长 64 字")
    private String nickname;

    @Size(max = 255, message = "头像地址过长")
    private String avatarUrl;

    @Size(max = 255, message = "简介最长 255 字")
    private String bio;
}
