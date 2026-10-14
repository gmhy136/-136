package com.cloudrecord.service.impl;

import com.cloudrecord.service.BadgeService;
import com.cloudrecord.service.OverviewService;
import com.cloudrecord.vo.BadgeVO;
import com.cloudrecord.vo.OverviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 成就徽章实现：复用主页统计，按阈值判定解锁。
 */
@Service
@RequiredArgsConstructor
public class BadgeServiceImpl implements BadgeService {

    private final OverviewService overviewService;

    @Override
    public List<BadgeVO> badges(Long userId) {
        OverviewVO ov = overviewService.overview(userId);
        return List.of(
                badge("first-record", "初", "初次记录", "写下第一张卡", ov.getRecordCount() >= 1),
                badge("ten-cards", "十", "十张卡片", "写满 10 张", ov.getRecordCount() >= 10),
                badge("streak-7", "七", "连续 7 天", "坚持打卡一周", ov.getStreakDays() >= 7),
                badge("ideal-done", "成", "理想达成", "完成第一个理想", ov.getIdealDoneCount() >= 1),
                badge("time-traveler", "旅", "时空旅人", "开启一封胶囊", ov.getCapsuleOpenedCount() >= 1),
                badge("full-moon", "满", "满月记录", "连续记录 30 天", ov.getRecordDays() >= 30));
    }

    private BadgeVO badge(String key, String seal, String name, String desc, boolean unlocked) {
        return BadgeVO.builder()
                .key(key).seal(seal).name(name).desc(desc).unlocked(unlocked)
                .build();
    }
}
