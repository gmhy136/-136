package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 心情天气月报：某月每天的心情天气点 + 晴/多云/雨计数 + 一句话小结。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoodReportVO {

    /** 月份，格式 yyyy-MM。 */
    private String month;

    /** 天 → 天气图标（md-sunny 等）。 */
    private Map<Integer, String> days;

    private int sunnyCount;

    private int cloudyCount;

    private int rainyCount;

    private String quote;
}
