package com.cloudrecord.service.impl;

import com.cloudrecord.service.OverviewService;
import com.cloudrecord.vo.BadgeVO;
import com.cloudrecord.vo.OverviewVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BadgeServiceImplTest {

    @Mock
    private OverviewService overviewService;

    @InjectMocks
    private BadgeServiceImpl badgeService;

    @Test
    void badges_allUnlockedWhenThresholdsMet() {
        when(overviewService.overview(1L)).thenReturn(OverviewVO.builder()
                .recordCount(12).recordDays(31).streakDays(8)
                .idealDoneCount(2).capsuleOpenedCount(3).build());

        List<BadgeVO> badges = badgeService.badges(1L);

        assertThat(badges).hasSize(6);
        assertThat(badges).allMatch(BadgeVO::isUnlocked);
    }

    @Test
    void badges_partiallyUnlockedByThresholds() {
        when(overviewService.overview(1L)).thenReturn(OverviewVO.builder()
                .recordCount(3).recordDays(3).streakDays(0)
                .idealDoneCount(0).capsuleOpenedCount(0).build());

        List<BadgeVO> badges = badgeService.badges(1L);

        assertThat(badges).extracting(BadgeVO::getKey).containsExactly(
                "first-record", "ten-cards", "streak-7", "ideal-done", "time-traveler", "full-moon");
        assertThat(badges.get(0).getSeal()).isEqualTo("初");
        assertThat(badges.get(0).getName()).isEqualTo("初次记录");
        assertThat(badges.get(2).getName()).isEqualTo("连续 7 天");
        assertThat(badges.get(0).isUnlocked()).isTrue();   // 初次记录 3 >= 1
        assertThat(badges.get(1).isUnlocked()).isFalse();  // 十张卡片
        assertThat(badges.get(2).isUnlocked()).isFalse();  // 连续七天
        assertThat(badges.get(3).isUnlocked()).isFalse();  // 理想达成
        assertThat(badges.get(4).isUnlocked()).isFalse();  // 时空旅人
        assertThat(badges.get(5).isUnlocked()).isFalse();  // 满月记录
    }
}
