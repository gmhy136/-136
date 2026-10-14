package com.cloudrecord.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 扩写请求。
 */
@Data
public class ExpandRequest {

    @NotBlank(message = "内容不能为空")
    @Size(max = 5000, message = "内容过长")
    private String content;

    private Integer moodId;
}
