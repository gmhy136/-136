package com.cloudrecord.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 写信给未来自己请求。
 */
@Data
public class CreateCapsuleRequest {

    @Size(max = 128, message = "标题最多 128 字")
    private String title;

    @NotBlank(message = "信的内容不能为空")
    private String content;

    @NotNull(message = "开启日期不能为空")
    private LocalDate openDate;
}
