package com.cloudrecord.vo;

import lombok.Data;

/**
 * 某月内「第几天 → 心情ID」投影行（一天多条记录取最后一条心情）。
 */
@Data
public class MoodDayRow {

    private Integer day;

    private Integer moodId;
}
