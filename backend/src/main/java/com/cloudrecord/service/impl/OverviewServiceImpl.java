package com.cloudrecord.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudrecord.common.BizException;
import com.cloudrecord.entity.CheckIn;
import com.cloudrecord.entity.User;
import com.cloudrecord.mapper.CheckInMapper;
import com.cloudrecord.mapper.RecordCardMapper;
import com.cloudrecord.mapper.UserMapper;
import com.cloudrecord.service.CapsuleService;
import com.cloudrecord.service.IdealService;
import com.cloudrecord.service.OverviewService;
import com.cloudrecord.service.RecordService;
import com.cloudrecord.vo.CapsuleVO;
import com.cloudrecord.vo.IdealVO;
import com.cloudrecord.vo.OverviewVO;
import com.cloudrecord.vo.PageVO;
import com.cloudrecord.vo.RecordVO;
import com.cloudrecord.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 个人主页聚合实现：复用各模块 Service 组装视图。
 */
@Service
@RequiredArgsConstructor
public class OverviewServiceImpl implements OverviewService {

    private static final int RECENT_LIMIT = 5;
    private static final String STATUS_DONE = "DONE";

    private final UserMapper userMapper;
    private final RecordCardMapper recordCardMapper;
    private final CheckInMapper checkInMapper;
    private final RecordService recordService;
    private final IdealService idealService;
    private final CapsuleService capsuleService;

    @Override
    public OverviewVO overview(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BizException.notFound("用户不存在");
        }

        PageVO<RecordVO> recent = recordService.list(userId, 1, RECENT_LIMIT);
        List<IdealVO> ideals = idealService.list(userId);
        List<CapsuleVO> capsules = capsuleService.list(userId);
        List<CheckIn> checkIns = checkInMapper.selectList(new LambdaQueryWrapper<CheckIn>()
                .eq(CheckIn::getUserId, userId));

        Set<LocalDate> checkDates = checkIns.stream()
                .map(CheckIn::getCheckDate)
                .collect(Collectors.toSet());
        LocalDate today = LocalDate.now();

        return OverviewVO.builder()
                .user(UserVO.from(user))
                .recordCount((int) recent.getTotal())
                .recordDays(recordCardMapper.countDistinctDays(userId))
                .streakDays(globalStreak(checkDates))
                .monthCheckInCount((int) checkIns.stream()
                        .filter(c -> c.getCheckDate().getYear() == today.getYear()
                                && c.getCheckDate().getMonth() == today.getMonth())
                        .count())
                .idealCount(ideals.size())
                .idealDoneCount((int) ideals.stream()
                        .filter(i -> STATUS_DONE.equals(i.getStatus()))
                        .count())
                .capsuleCount(capsules.size())
                .capsuleOpenedCount((int) capsules.stream()
                        .filter(CapsuleVO::isOpened)
                        .count())
                .recentRecords(recent.getRecords())
                .ideals(ideals)
                .build();
    }

    /** 全局连续打卡天数（有任意理想打卡即算一天，截至今天或昨天）。 */
    private int globalStreak(Set<LocalDate> dates) {
        if (dates.isEmpty()) {
            return 0;
        }
        LocalDate cursor = LocalDate.now();
        if (!dates.contains(cursor)) {
            cursor = cursor.minusDays(1);
        }
        int count = 0;
        while (dates.contains(cursor)) {
            count++;
            cursor = cursor.minusDays(1);
        }
        return count;
    }
}
