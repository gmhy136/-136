package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 理想卡列表视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdealVO {

    private Long id;
    private String title;
    private String description;
    private String visibility;
    private String status;
    private LocalDate targetDate;
    private int taskCount;
    private int completedTaskCount;
}
