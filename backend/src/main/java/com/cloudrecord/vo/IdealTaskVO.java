package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 理想任务视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdealTaskVO {

    private Long id;
    private String content;
    private boolean completed;
    private int sortOrder;
}
