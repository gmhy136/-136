package com.cloudrecord.service;

import com.cloudrecord.dto.AddTaskRequest;
import com.cloudrecord.dto.CreateIdealRequest;
import com.cloudrecord.vo.CalendarVO;
import com.cloudrecord.vo.IdealDetailVO;
import com.cloudrecord.vo.IdealTaskVO;
import com.cloudrecord.vo.IdealVO;

import java.util.List;

/**
 * 理想卡服务：创建 / 任务 / 打卡 / 日历（全部按 user_id 归属校验）。
 */
public interface IdealService {

    List<IdealVO> list(Long userId);

    IdealVO create(Long userId, CreateIdealRequest request);

    IdealDetailVO getDetail(Long userId, Long id);

    void delete(Long userId, Long id);

    IdealTaskVO addTask(Long userId, Long idealId, AddTaskRequest request);

    /** 切换任务完成态；全部任务完成时理想自动置为 DONE。 */
    IdealTaskVO toggleTask(Long userId, Long idealId, Long taskId);

    /** 今日打卡（幂等）。返回刷新后的详情。 */
    IdealDetailVO checkIn(Long userId, Long idealId);

    CalendarVO calendar(Long userId, Long idealId, String month);
}
