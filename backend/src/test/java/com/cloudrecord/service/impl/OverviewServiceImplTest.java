package com.cloudrecord.service.impl;

import com.cloudrecord.common.BizException;
import com.cloudrecord.entity.CheckIn;
import com.cloudrecord.entity.User;
import com.cloudrecord.mapper.CheckInMapper;
import com.cloudrecord.mapper.RecordCardMapper;
import com.cloudrecord.mapper.UserMapper;
import com.cloudrecord.service.CapsuleService;
import com.cloudrecord.service.IdealService;
import com.cloudrecord.service.RecordService;
import com.cloudrecord.vo.CapsuleVO;
import com.cloudrecord.vo.IdealVO;
import com.cloudrecord.vo.OverviewVO;
import com.cloudrecord.vo.PageVO;
import com.cloudrecord.vo.RecordVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OverviewServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RecordCardMapper recordCardMapper;

    @Mock
    private CheckInMapper checkInMapper;

    @Mock
    private RecordService recordService;

    @Mock
    private IdealService idealService;

    @Mock
    private CapsuleService capsuleService;

    @InjectMocks
    private OverviewServiceImpl overviewService;

    private User user() {
        User u = new User();
        u.setId(1L);
        u.setEmail("a@b.c");
        u.setNickname("小明");
        u.setBio("记录生活");
        return u;
    }

    private RecordVO record(Long id) {
        return RecordVO.builder().id(id).content("记录" + id).build();
    }

    private IdealVO ideal(Long id, String status) {
        return IdealVO.builder().id(id).title("理想" + id).status(status).build();
    }

    private CapsuleVO capsule(Long id, boolean opened) {
        return CapsuleVO.builder().id(id).isOpened(opened).build();
    }

    private CheckIn checkIn(LocalDate date) {
        CheckIn c = new CheckIn();
        c.setUserId(1L);
        c.setCheckDate(date);
        return c;
    }

    @Test
    void overview_aggregatesCounts() {
        LocalDate today = LocalDate.now();
        when(userMapper.selectById(1L)).thenReturn(user());
        when(recordCardMapper.countDistinctDays(1L)).thenReturn(10);
        when(recordService.list(1L, 1, 5))
                .thenReturn(new PageVO<>(List.of(record(1L), record(2L)), 12L, 1L, 5L, true));
        when(idealService.list(1L)).thenReturn(List.of(
                ideal(1L, "ACTIVE"), ideal(2L, "DONE"), ideal(3L, "ACTIVE")));
        when(capsuleService.list(1L)).thenReturn(List.of(
                capsule(1L, true), capsule(2L, false), capsule(3L, true), capsule(4L, false)));
        when(checkInMapper.selectList(any())).thenReturn(List.of(checkIn(today)));

        OverviewVO vo = overviewService.overview(1L);

        assertThat(vo.getUser().getNickname()).isEqualTo("小明");
        assertThat(vo.getRecordCount()).isEqualTo(12);
        assertThat(vo.getRecordDays()).isEqualTo(10);
        assertThat(vo.getRecentRecords()).hasSize(2);
        assertThat(vo.getIdealCount()).isEqualTo(3);
        assertThat(vo.getIdealDoneCount()).isEqualTo(1);
        assertThat(vo.getCapsuleCount()).isEqualTo(4);
        assertThat(vo.getCapsuleOpenedCount()).isEqualTo(2);
        assertThat(vo.getStreakDays()).isEqualTo(1);
        assertThat(vo.getMonthCheckInCount()).isEqualTo(1);
        assertThat(vo.getIdeals()).hasSize(3);
    }

    @Test
    void overview_streakCountsConsecutiveDays() {
        LocalDate today = LocalDate.now();
        when(userMapper.selectById(1L)).thenReturn(user());
        when(recordService.list(1L, 1, 5)).thenReturn(new PageVO<>(List.of(), 0L, 1L, 5L, false));
        when(idealService.list(1L)).thenReturn(List.of());
        when(capsuleService.list(1L)).thenReturn(List.of());
        when(checkInMapper.selectList(any()))
                .thenReturn(List.of(checkIn(today), checkIn(today.minusDays(1))));

        OverviewVO vo = overviewService.overview(1L);

        assertThat(vo.getStreakDays()).isEqualTo(2);
    }

    @Test
    void overview_streakStartsFromYesterdayWhenTodayMissing() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        when(userMapper.selectById(1L)).thenReturn(user());
        when(recordService.list(1L, 1, 5)).thenReturn(new PageVO<>(List.of(), 0L, 1L, 5L, false));
        when(idealService.list(1L)).thenReturn(List.of());
        when(capsuleService.list(1L)).thenReturn(List.of());
        when(checkInMapper.selectList(any())).thenReturn(List.of(checkIn(yesterday)));

        OverviewVO vo = overviewService.overview(1L);

        assertThat(vo.getStreakDays()).isEqualTo(1);
    }

    @Test
    void overview_noCheckIn_zeroStreak() {
        when(userMapper.selectById(1L)).thenReturn(user());
        when(recordService.list(1L, 1, 5)).thenReturn(new PageVO<>(List.of(), 0L, 1L, 5L, false));
        when(idealService.list(1L)).thenReturn(List.of());
        when(capsuleService.list(1L)).thenReturn(List.of());
        when(checkInMapper.selectList(any())).thenReturn(List.of());

        OverviewVO vo = overviewService.overview(1L);

        assertThat(vo.getStreakDays()).isZero();
        assertThat(vo.getMonthCheckInCount()).isZero();
    }

    @Test
    void overview_userNotFound_throws404() {
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> overviewService.overview(999L))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(404));
    }
}
