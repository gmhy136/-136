package com.cloudrecord.service;

import com.cloudrecord.vo.MoodReportVO;

/**
 * 亮点报表：心情天气月报。
 */
public interface ReportService {

    /** 某月心情天气月报；month 传 yyyy-MM，缺省为当月。 */
    MoodReportVO moodReport(Long userId, String month);
}
