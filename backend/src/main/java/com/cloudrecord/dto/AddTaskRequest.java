package com.cloudrecord.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 手动添加理想任务请求。
 */
@Data
public class AddTaskRequest {

    @NotBlank(message = "任务内容不能为空")
    @Size(max = 255, message = "任务最多 255 字")
    private String content;
}
