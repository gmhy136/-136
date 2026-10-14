package com.cloudrecord.service.impl;

import com.cloudrecord.common.BizException;
import com.cloudrecord.entity.Mood;
import com.cloudrecord.mapper.MoodMapper;
import com.cloudrecord.mapper.RecordCardMapper;
import com.cloudrecord.service.ReportService;
import com.cloudrecord.vo.MoodDayRow;
import com.cloudrecord.vo.MoodReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 心情天气月报实现：按天取最后一条心情 → 映射晴/多云/雨 → 计数 + 一句话小结。
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final String SUNNY_QUOTE = "这个月阳光很足，你也在好好生活。";
    private static final String CLOUDY_QUOTE = "日子不总是晴天，平静也是一种收获。";
    private static final String RAINY_QUOTE = "有些雨会落在心上，但也会慢慢过去。";
    private static final String EMPTY_QUOTE = "这个月还没有心情记录，从今天开始吧。";

    private final RecordCardMapper recordCardMapper;
    private final MoodMapper moodMapper;

    @Override
    public MoodReportVO moodReport(Long userId, String month) {
        YearMonth ym = parseMonth(month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<MoodDayRow> rows = recordCardMapper.selectMoodDayRows(userId, start, end);
        Map<Integer, String> icons = weatherIconMap();

        Map<Integer, String> days = new LinkedHashMap<>();
        int sunny = 0;
        int cloudy = 0;
        int rainy = 0;
        for (MoodDayRow row : rows) {
            String icon = icons.get(row.getMoodId());
            if (icon == null) {
                continue;
            }
            days.put(row.getDay(), icon);
            if (isSunny(icon)) {
                sunny++;
            } else if (isCloudy(icon)) {
                cloudy++;
            } else {
                rainy++;
            }
        }

        String quote = EMPTY_QUOTE;
        if (!days.isEmpty()) {
            if (sunny >= cloudy && sunny >= rainy) {
                quote = SUNNY_QUOTE;
            } else if (cloudy >= rainy) {
                quote = CLOUDY_QUOTE;
            } else {
                quote = RAINY_QUOTE;
            }
        }

        return MoodReportVO.builder()
                .month(ym.toString())
                .days(days)
                .sunnyCount(sunny)
                .cloudyCount(cloudy)
                .rainyCount(rainy)
                .quote(quote)
                .build();
    }

    private YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw BizException.badRequest("月份格式应为 yyyy-MM");
        }
    }

    private Map<Integer, String> weatherIconMap() {
        Map<Integer, String> map = new HashMap<>();
        for (Mood mood : moodMapper.selectList(null)) {
            map.put(mood.getId(), mood.getWeatherIcon());
        }
        return map;
    }

    private boolean isSunny(String icon) {
        return "md-sunny".equals(icon) || "md-joy".equals(icon);
    }

    private boolean isCloudy(String icon) {
        return "md-calm".equals(icon);
    }
}
