package com.cloudrecord.controller;

import com.cloudrecord.common.Result;
import com.cloudrecord.security.UserContext;
import com.cloudrecord.service.ReportService;
import com.cloudrecord.service.TimelineService;
import com.cloudrecord.vo.MoodReportVO;
import com.cloudrecord.vo.TimelineEventVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 亮点报表：心情天气月报 + 成长时间线。
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final TimelineService timelineService;

    @GetMapping("/mood")
    public Result<MoodReportVO> mood(@RequestParam(required = false) String month) {
        return Result.ok(reportService.moodReport(UserContext.requireUserId(), month));
    }

    @GetMapping("/timeline")
    public Result<List<TimelineEventVO>> timeline() {
        return Result.ok(timelineService.timeline(UserContext.requireUserId()));
    }
}
