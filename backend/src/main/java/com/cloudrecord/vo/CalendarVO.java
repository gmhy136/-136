package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 打卡日历视图（按查询月份聚合）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarVO {

    /** 查询月份，如 2026-10。 */
    private String month;

    /** 该月已打卡的「日号」列表。 */
    private List<Integer> days;

    private int streakDays;

    /** 该月打卡天数。 */
    private int monthCount;
}
