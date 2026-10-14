package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 理想详情视图（含任务、打卡聚合）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdealDetailVO {

    private Long id;
    private String title;
    private String description;
    private String visibility;
    private String status;
    private LocalDate targetDate;
    private int taskCount;
    private int completedTaskCount;
    private List<IdealTaskVO> tasks;
    private boolean todayChecked;
    private int streakDays;
    private int monthCheckInDays;
    private List<LocalDate> checkInDates;
}
