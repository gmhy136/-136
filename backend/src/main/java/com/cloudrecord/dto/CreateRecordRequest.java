package com.cloudrecord.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateRecordRequest {

    @NotBlank(message = "内容不能为空")
    @Size(max = 10000, message = "内容过长")
    private String content;

    private Integer moodId;

    private String styleKey;

    /** 原创 / AI润色 / AI扩写（AI 采纳时由前端传入）。 */
    private String aiType;

    private List<String> imageUrls;
}
