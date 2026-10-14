package com.cloudrecord.service.impl;

import com.cloudrecord.common.BizException;
import com.cloudrecord.dto.AddTaskRequest;
import com.cloudrecord.dto.CreateIdealRequest;
import com.cloudrecord.entity.CheckIn;
import com.cloudrecord.entity.Ideal;
import com.cloudrecord.entity.IdealTask;
import com.cloudrecord.mapper.CheckInMapper;
import com.cloudrecord.mapper.IdealMapper;
import com.cloudrecord.mapper.IdealTaskMapper;
import com.cloudrecord.vo.CalendarVO;
import com.cloudrecord.vo.IdealDetailVO;
import com.cloudrecord.vo.IdealTaskVO;
import com.cloudrecord.vo.IdealVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdealServiceImplTest {

    @Mock
    private IdealMapper idealMapper;

    @Mock
    private IdealTaskMapper idealTaskMapper;

    @Mock
    private CheckInMapper checkInMapper;

    @InjectMocks
    private IdealServiceImpl idealService;

    private Ideal ideal(Long id) {
        Ideal i = new Ideal();
        i.setId(id);
        i.setUserId(1L);
        i.setTitle("学会弹一首完整的歌");
        i.setDescription("把一首曲子练到能完整弹下来");
        i.setVisibility("PRIVATE");
        i.setStatus("ACTIVE");
        i.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        return i;
    }

    private IdealTask task(Long id, Long idealId, int completed, int sort) {
        IdealTask t = new IdealTask();
        t.setId(id);
        t.setIdealId(idealId);
        t.setContent("任务" + id);
        t.setIsCompleted(completed);
        t.setSortOrder(sort);
        return t;
    }

    private CheckIn checkIn(Long idealId, LocalDate date) {
        CheckIn c = new CheckIn();
        c.setId(1L);
        c.setIdealId(idealId);
        c.setUserId(1L);
        c.setCheckDate(date);
        c.setStatus(1);
        return c;
    }

    @Test
    void list_attachesTaskCounts() {
        when(idealMapper.selectList(any())).thenReturn(List.of(ideal(1L), ideal(2L)));
        when(idealTaskMapper.selectList(any())).thenReturn(List.of(
                task(10L, 1L, 1, 0),
                task(11L, 1L, 0, 1),
                task(12L, 2L, 0, 0)));

        List<IdealVO> list = idealService.list(1L);

        assertThat(list).hasSize(2);
        IdealVO first = list.get(0);
        assertThat(first.getTaskCount()).isEqualTo(2);
        assertThat(first.getCompletedTaskCount()).isEqualTo(1);
        assertThat(list.get(1).getTaskCount()).isEqualTo(1);
        assertThat(list.get(1).getCompletedTaskCount()).isZero();
    }

    @Test
    void create_defaultsVisibilityPrivateAndStatusActive() {
        doAnswer(inv -> {
            Ideal i = inv.getArgument(0);
            i.setId(10L);
            return 1;
        }).when(idealMapper).insert(any(Ideal.class));

        CreateIdealRequest req = new CreateIdealRequest();
        req.setTitle("  学会弹歌  ");

        IdealVO vo = idealService.create(1L, req);

        assertThat(vo.getId()).isEqualTo(10L);
        assertThat(vo.getTitle()).isEqualTo("学会弹歌");
        assertThat(vo.getVisibility()).isEqualTo("PRIVATE");
        assertThat(vo.getStatus()).isEqualTo("ACTIVE");
        assertThat(vo.getTaskCount()).isZero();
    }

    @Test
    void getDetail_assemblesTasksAndCheckIn() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        when(idealMapper.selectOne(any())).thenReturn(ideal(1L));
        when(idealTaskMapper.selectList(any())).thenReturn(List.of(task(10L, 1L, 1, 0), task(11L, 1L, 0, 1)));
        when(checkInMapper.selectList(any())).thenReturn(List.of(checkIn(1L, yesterday), checkIn(1L, today)));

        IdealDetailVO vo = idealService.getDetail(1L, 1L);

        assertThat(vo.getTaskCount()).isEqualTo(2);
        assertThat(vo.getCompletedTaskCount()).isEqualTo(1);
        assertThat(vo.getTasks()).hasSize(2);
        assertThat(vo.isTodayChecked()).isTrue();
        assertThat(vo.getStreakDays()).isEqualTo(2);
        assertThat(vo.getMonthCheckInDays()).isEqualTo(2);
    }

    @Test
    void getDetail_notOwnedThrows404() {
        when(idealMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> idealService.getDetail(1L, 999L))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(404));
    }

    @Test
    void delete_logicDelete() {
        when(idealMapper.selectOne(any())).thenReturn(ideal(1L));

        idealService.delete(1L, 1L);

        verify(idealMapper).deleteById(1L);
    }

    @Test
    void addTask_setsSortOrderAndContent() {
        when(idealMapper.selectOne(any())).thenReturn(ideal(1L));
        when(idealTaskMapper.selectCount(any())).thenReturn(2L);
        when(idealMapper.selectById(1L)).thenReturn(ideal(1L));
        when(idealTaskMapper.selectList(any())).thenReturn(List.of());

        AddTaskRequest req = new AddTaskRequest();
        req.setContent("  练熟左手和弦  ");

        IdealTaskVO vo = idealService.addTask(1L, 1L, req);

        assertThat(vo.getContent()).isEqualTo("练熟左手和弦");
        assertThat(vo.getSortOrder()).isEqualTo(2);
        assertThat(vo.isCompleted()).isFalse();
        verify(idealTaskMapper).insert(any(IdealTask.class));
    }

    @Test
    void toggleTask_completesAll_autoDone() {
        Ideal ideal = ideal(1L);
        when(idealMapper.selectOne(any())).thenReturn(ideal);
        when(idealTaskMapper.selectById(10L)).thenReturn(task(10L, 1L, 0, 0));
        when(idealMapper.selectById(1L)).thenReturn(ideal);
        when(idealTaskMapper.selectList(any())).thenReturn(List.of(task(10L, 1L, 1, 0)));

        IdealTaskVO vo = idealService.toggleTask(1L, 1L, 10L);

        assertThat(vo.isCompleted()).isTrue();
        verify(idealMapper).updateById(any(Ideal.class));
    }

    @Test
    void toggleTask_uncomplete_revertsToActive() {
        Ideal done = ideal(1L);
        done.setStatus("DONE");
        when(idealMapper.selectOne(any())).thenReturn(done);
        when(idealTaskMapper.selectById(10L)).thenReturn(task(10L, 1L, 1, 0));
        when(idealMapper.selectById(1L)).thenReturn(done);
        when(idealTaskMapper.selectList(any())).thenReturn(List.of(task(10L, 1L, 0, 0)));

        IdealTaskVO vo = idealService.toggleTask(1L, 1L, 10L);

        assertThat(vo.isCompleted()).isFalse();
        verify(idealMapper).updateById(any(Ideal.class));
    }

    @Test
    void toggleTask_taskNotOwnedThrows404() {
        when(idealMapper.selectOne(any())).thenReturn(ideal(1L));
        when(idealTaskMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> idealService.toggleTask(1L, 1L, 999L))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(404));
    }

    @Test
    void checkIn_insertsAndReturnsDetail() {
        LocalDate today = LocalDate.now();
        when(idealMapper.selectOne(any())).thenReturn(ideal(1L));
        when(checkInMapper.selectOne(any())).thenReturn(null);
        when(idealTaskMapper.selectList(any())).thenReturn(List.of());
        when(checkInMapper.selectList(any())).thenReturn(List.of(checkIn(1L, today)));

        IdealDetailVO vo = idealService.checkIn(1L, 1L);

        assertThat(vo.isTodayChecked()).isTrue();
        assertThat(vo.getStreakDays()).isEqualTo(1);
        verify(checkInMapper).insert(any(CheckIn.class));
    }

    @Test
    void checkIn_alreadyChecked_noDuplicateInsert() {
        LocalDate today = LocalDate.now();
        when(idealMapper.selectOne(any())).thenReturn(ideal(1L));
        when(checkInMapper.selectOne(any())).thenReturn(checkIn(1L, today));
        when(idealTaskMapper.selectList(any())).thenReturn(List.of());
        when(checkInMapper.selectList(any())).thenReturn(List.of(checkIn(1L, today)));

        idealService.checkIn(1L, 1L);

        verify(checkInMapper, never()).insert(any(CheckIn.class));
    }

    @Test
    void calendar_aggregatesMonthAndStreak() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        when(idealMapper.selectOne(any())).thenReturn(ideal(1L));
        when(checkInMapper.selectList(any())).thenReturn(List.of(checkIn(1L, yesterday), checkIn(1L, today)));

        CalendarVO vo = idealService.calendar(1L, 1L, YearMonth.now().toString());

        assertThat(vo.getMonth()).isEqualTo(YearMonth.now().toString());
        assertThat(vo.getDays()).contains(today.getDayOfMonth());
        assertThat(vo.getMonthCount()).isEqualTo(2);
        assertThat(vo.getStreakDays()).isEqualTo(2);
    }
}
