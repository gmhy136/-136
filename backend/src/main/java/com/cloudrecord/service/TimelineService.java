package com.cloudrecord.service;

import com.cloudrecord.vo.TimelineEventVO;

import java.util.List;

/**
 * 亮点报表：成长时间线。
 */
public interface TimelineService {

    /** 汇总记录 / 理想 / 胶囊事件，按日期倒序。 */
    List<TimelineEventVO> timeline(Long userId);
}
