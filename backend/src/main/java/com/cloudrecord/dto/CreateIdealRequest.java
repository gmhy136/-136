package com.cloudrecord.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建理想请求。
 */
@Data
public class CreateIdealRequest {

    @NotBlank(message = "理想标题不能为空")
    @Size(max = 128, message = "标题最多 128 字")
    private String title;

    @Size(max = 2000, message = "描述最多 2000 字")
    private String description;

    /** PRIVATE / PUBLIC，缺省 PRIVATE（公开为二期预留）。 */
    private String visibility;

    private LocalDate targetDate;
}
