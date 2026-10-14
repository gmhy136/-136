package com.cloudrecord.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRecordRequest {

    @NotBlank(message = "内容不能为空")
    @Size(max = 10000, message = "内容过长")
    private String content;

    private Integer moodId;

    private String styleKey;

    private String aiType;

    /** 编辑时整体替换图片列表。 */
    private List<String> imageUrls;
}
