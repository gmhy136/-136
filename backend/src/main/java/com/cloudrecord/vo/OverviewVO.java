package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 个人主页聚合视图（卡片 + 理想 + 统计）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverviewVO {

    private UserVO user;
    private int recordCount;
    private int recordDays;
    private int streakDays;
    private int monthCheckInCount;
    private int idealCount;
    private int idealDoneCount;
    private int capsuleCount;
    private int capsuleOpenedCount;
    private List<RecordVO> recentRecords;
    private List<IdealVO> ideals;
}
