package com.cloudrecord.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建 / 更新问答游戏请求。
 */
@Data
public class CreateQuizRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 128, message = "标题最多 128 字")
    private String title;

    @Size(max = 512, message = "简介最多 512 字")
    private String description;

    @NotEmpty(message = "至少需要一道题")
    private List<QuestionInput> questions;
}
