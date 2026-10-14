package com.cloudrecord.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudrecord.entity.Ideal;
import com.cloudrecord.entity.IdealTask;
import com.cloudrecord.entity.Mood;
import com.cloudrecord.entity.RecordCard;
import com.cloudrecord.entity.TimeCapsule;
import com.cloudrecord.mapper.IdealMapper;
import com.cloudrecord.mapper.IdealTaskMapper;
import com.cloudrecord.mapper.MoodMapper;
import com.cloudrecord.mapper.RecordCardMapper;
import com.cloudrecord.mapper.TimeCapsuleMapper;
import com.cloudrecord.vo.TimelineEventVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimelineServiceImplTest {

    @Mock
    private RecordCardMapper recordCardMapper;

    @Mock
    private IdealMapper idealMapper;

    @Mock
    private IdealTaskMapper idealTaskMapper;

    @Mock
    private TimeCapsuleMapper timeCapsuleMapper;

    @Mock
    private MoodMapper moodMapper;

    @InjectMocks
    private TimelineServiceImpl timelineService;

    private Mood mood(Integer id, String name) {
        Mood m = new Mood();
        m.setId(id);
        m.setName(name);
        return m;
    }

    private RecordCard record(String content, Integer moodId, LocalDate date) {
        RecordCard r = new RecordCard();
        r.setId(1L);
        r.setContent(content);
        r.setMoodId(moodId);
        r.setCreatedAt(date.atStartOfDay());
        return r;
    }

    private Ideal ideal(Long id, String title, String status, LocalDate created, LocalDate updated) {
        Ideal i = new Ideal();
        i.setId(id);
        i.setTitle(title);
        i.setStatus(status);
        i.setCreatedAt(created.atStartOfDay());
        i.setUpdatedAt(updated.atStartOfDay());
        return i;
    }

    private IdealTask task(Long idealId, int completed) {
        IdealTask t = new IdealTask();
        t.setIdealId(idealId);
        t.setIsCompleted(completed);
        return t;
    }

    private TimeCapsule capsule(String title, LocalDate openDate) {
        TimeCapsule c = new TimeCapsule();
        c.setId(1L);
        c.setTitle(title);
        c.setOpenDate(openDate);
        c.setIsOpened(1);
        return c;
    }

    @Test
    void timeline_aggregatesAllTypes_sortedDesc() {
        when(moodMapper.selectList(any())).thenReturn(List.of(mood(1, "开心")));

        Page<RecordCard> page = new Page<>(1, 30);
        page.setRecords(List.of(record("今天阳光很好", 1, LocalDate.of(2026, 10, 10))));
        when(recordCardMapper.selectPage(any(), any())).thenReturn(page);

        when(idealMapper.selectList(any())).thenReturn(List.of(
                ideal(1L, "学会弹琴", "ACTIVE", LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 5)),
                ideal(2L, "跑完半马", "DONE", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 12))));
        when(idealTaskMapper.selectList(any()))
                .thenReturn(List.of(task(1L, 1), task(1L, 1), task(1L, 0)));
        when(timeCapsuleMapper.selectList(any()))
                .thenReturn(List.of(capsule("写给一年后的自己", LocalDate.of(2026, 10, 11))));

        List<TimelineEventVO> events = timelineService.timeline(1L);

        assertThat(events).extracting(TimelineEventVO::getType)
                .containsExactlyInAnyOrder("RECORD", "IDEAL_START", "IDEAL_START", "IDEAL_DONE", "CAPSULE_OPEN", "IDEAL_ACTIVE");
        assertThat(events).isSortedAccordingTo(
                Comparator.comparing(TimelineEventVO::getDate).reversed());

        TimelineEventVO record = events.stream()
                .filter(e -> "RECORD".equals(e.getType())).findFirst().orElseThrow();
        assertThat(record.getLabel()).isEqualTo("开心");
        assertThat(record.getDetail()).isEqualTo("今天阳光很好");

        TimelineEventVO active = events.stream()
                .filter(e -> "IDEAL_ACTIVE".equals(e.getType())).findFirst().orElseThrow();
        assertThat(active.getLabel()).isEqualTo("理想进行中");
        assertThat(active.getDetail()).isEqualTo("学会弹琴，已完成 2 步");
        assertThat(active.getDate()).isEqualTo(LocalDate.now());

        TimelineEventVO start = events.stream()
                .filter(e -> "IDEAL_START".equals(e.getType()) && e.getDetail().contains("学会弹琴"))
                .findFirst().orElseThrow();
        assertThat(start.getLabel()).isEqualTo("开始");
        assertThat(start.getDetail()).isEqualTo("种下「学会弹琴」的理想");

        TimelineEventVO capsule = events.stream()
                .filter(e -> "CAPSULE_OPEN".equals(e.getType())).findFirst().orElseThrow();
        assertThat(capsule.getLabel()).isEqualTo("胶囊开启");
    }

    @Test
    void timeline_truncatesLongContent() {
        when(moodMapper.selectList(any())).thenReturn(List.of());

        String longContent = "很".repeat(80);
        Page<RecordCard> page = new Page<>(1, 30);
        page.setRecords(List.of(record(longContent, null, LocalDate.of(2026, 10, 10))));
        when(recordCardMapper.selectPage(any(), any())).thenReturn(page);
        when(idealMapper.selectList(any())).thenReturn(List.of());
        when(timeCapsuleMapper.selectList(any())).thenReturn(List.of());

        List<TimelineEventVO> events = timelineService.timeline(1L);

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getDetail()).hasSize(61).endsWith("…");
    }
}
