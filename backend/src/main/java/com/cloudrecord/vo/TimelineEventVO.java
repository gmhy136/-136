package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 成长时间线事件（记录 / 种下理想 / 达成理想 / 开启胶囊）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEventVO {

    private LocalDate date;

    /** RECORD / IDEAL_START / IDEAL_DONE / CAPSULE_OPEN。 */
    private String type;

    private String label;

    private String detail;
}
