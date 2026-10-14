package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 成就徽章（是否解锁由统计阈值决定）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeVO {

    private String key;

    /** 徽章印章字（单字，如「初」「十」）。 */
    private String seal;

    private String name;

    private String desc;

    private boolean unlocked;
}
