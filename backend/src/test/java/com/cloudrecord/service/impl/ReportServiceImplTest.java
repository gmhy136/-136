package com.cloudrecord.service.impl;

import com.cloudrecord.common.BizException;
import com.cloudrecord.entity.Mood;
import com.cloudrecord.mapper.MoodMapper;
import com.cloudrecord.mapper.RecordCardMapper;
import com.cloudrecord.vo.MoodDayRow;
import com.cloudrecord.vo.MoodReportVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private RecordCardMapper recordCardMapper;

    @Mock
    private MoodMapper moodMapper;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Mood mood(Integer id, String icon) {
        Mood m = new Mood();
        m.setId(id);
        m.setWeatherIcon(icon);
        return m;
    }

    private MoodDayRow row(int day, int moodId) {
        MoodDayRow r = new MoodDayRow();
        r.setDay(day);
        r.setMoodId(moodId);
        return r;
    }

    private void seedMoods() {
        when(moodMapper.selectList(any())).thenReturn(List.of(
                mood(1, "md-sunny"), mood(2, "md-calm"), mood(3, "md-joy"),
                mood(4, "md-moon"), mood(5, "md-rain"), mood(6, "md-storm")));
    }

    @Test
    void moodReport_mapsIconsAndCounts() {
        seedMoods();
        when(recordCardMapper.selectMoodDayRows(any(), any(), any()))
                .thenReturn(List.of(row(1, 1), row(2, 2), row(3, 3), row(5, 6)));

        MoodReportVO vo = reportService.moodReport(1L, "2026-10");

        assertThat(vo.getMonth()).isEqualTo("2026-10");
        assertThat(vo.getDays())
                .containsEntry(1, "md-sunny")
                .containsEntry(2, "md-calm")
                .containsEntry(3, "md-joy")
                .containsEntry(5, "md-storm");
        assertThat(vo.getSunnyCount()).isEqualTo(2);   // md-sunny + md-joy
        assertThat(vo.getCloudyCount()).isEqualTo(1);  // md-calm
        assertThat(vo.getRainyCount()).isEqualTo(1);   // md-storm
        assertThat(vo.getQuote()).isNotEmpty();
    }

    @Test
    void moodReport_rainyDominant_returnsRainyQuote() {
        seedMoods();
        when(recordCardMapper.selectMoodDayRows(any(), any(), any()))
                .thenReturn(List.of(row(1, 5), row(2, 6), row(3, 1)));

        MoodReportVO vo = reportService.moodReport(1L, "2026-10");

        assertThat(vo.getRainyCount()).isEqualTo(2);
        assertThat(vo.getSunnyCount()).isEqualTo(1);
        assertThat(vo.getQuote()).contains("雨");
    }

    @Test
    void moodReport_empty_returnsEmptyQuote() {
        seedMoods();
        when(recordCardMapper.selectMoodDayRows(any(), any(), any())).thenReturn(List.of());

        MoodReportVO vo = reportService.moodReport(1L, "2026-10");

        assertThat(vo.getDays()).isEmpty();
        assertThat(vo.getSunnyCount()).isZero();
        assertThat(vo.getQuote()).contains("还没有");
    }

    @Test
    void moodReport_invalidMonth_throws400() {
        assertThatThrownBy(() -> reportService.moodReport(1L, "2026/10"))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(400));
    }
}
