package com.cloudrecord.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 理想拆解请求。
 */
@Data
public class SplitIdealRequest {

    @NotBlank(message = "理想标题不能为空")
    @Size(max = 128, message = "标题最多 128 字")
    private String title;

    @Size(max = 2000, message = "描述最多 2000 字")
    private String description;
}
