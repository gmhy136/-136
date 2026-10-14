package com.cloudrecord.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.cloudrecord.service.TimelineService;
import com.cloudrecord.vo.TimelineEventVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成长时间线实现：记录卡 + 理想（进行中 / 种下 / 达成）+ 已开启胶囊，按日期倒序。
 */
@Service
@RequiredArgsConstructor
public class TimelineServiceImpl implements TimelineService {

    private static final int RECORD_LIMIT = 30;
    private static final int DETAIL_MAX = 60;
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_DONE = "DONE";

    private final RecordCardMapper recordCardMapper;
    private final IdealMapper idealMapper;
    private final IdealTaskMapper idealTaskMapper;
    private final TimeCapsuleMapper timeCapsuleMapper;
    private final MoodMapper moodMapper;

    @Override
    public List<TimelineEventVO> timeline(Long userId) {
        List<TimelineEventVO> events = new ArrayList<>();
        Map<Integer, String> moodNames = moodNameMap();

        Page<RecordCard> page = recordCardMapper.selectPage(new Page<>(1, RECORD_LIMIT),
                new LambdaQueryWrapper<RecordCard>()
                        .eq(RecordCard::getUserId, userId)
                        .orderByDesc(RecordCard::getCreatedAt));
        for (RecordCard record : page.getRecords()) {
            if (record.getCreatedAt() == null) {
                continue;
            }
            String moodName = record.getMoodId() == null ? null : moodNames.get(record.getMoodId());
            events.add(TimelineEventVO.builder()
                    .date(record.getCreatedAt().toLocalDate())
                    .type("RECORD")
                    .label(moodName != null ? moodName : "记录")
                    .detail(truncate(record.getContent()))
                    .build());
        }

        List<Ideal> ideals = idealMapper.selectList(new LambdaQueryWrapper<Ideal>()
                .eq(Ideal::getUserId, userId));
        Map<Long, int[]> counts = taskCounts(ideals.stream()
                .filter(i -> STATUS_ACTIVE.equals(i.getStatus()))
                .map(Ideal::getId)
                .toList());
        LocalDate today = LocalDate.now();
        for (Ideal ideal : ideals) {
            if (STATUS_ACTIVE.equals(ideal.getStatus())) {
                int[] c = counts.getOrDefault(ideal.getId(), new int[2]);
                String detail = c[0] > 0
                        ? ideal.getTitle() + "，已完成 " + c[1] + " 步"
                        : ideal.getTitle() + "，还没有任务";
                events.add(TimelineEventVO.builder()
                        .date(today)
                        .type("IDEAL_ACTIVE")
                        .label("理想进行中")
                        .detail(detail)
                        .build());
            }
            if (ideal.getCreatedAt() != null) {
                events.add(TimelineEventVO.builder()
                        .date(ideal.getCreatedAt().toLocalDate())
                        .type("IDEAL_START")
                        .label("开始")
                        .detail("种下「" + ideal.getTitle() + "」的理想")
                        .build());
            }
            if (STATUS_DONE.equals(ideal.getStatus()) && ideal.getUpdatedAt() != null) {
                events.add(TimelineEventVO.builder()
                        .date(ideal.getUpdatedAt().toLocalDate())
                        .type("IDEAL_DONE")
                        .label("完成理想")
                        .detail(ideal.getTitle())
                        .build());
            }
        }

        List<TimeCapsule> capsules = timeCapsuleMapper.selectList(new LambdaQueryWrapper<TimeCapsule>()
                .eq(TimeCapsule::getUserId, userId)
                .eq(TimeCapsule::getIsOpened, 1));
        for (TimeCapsule capsule : capsules) {
            LocalDate date = capsule.getOpenDate() != null
                    ? capsule.getOpenDate()
                    : (capsule.getCreatedAt() != null ? capsule.getCreatedAt().toLocalDate() : null);
            if (date == null) {
                continue;
            }
            events.add(TimelineEventVO.builder()
                    .date(date)
                    .type("CAPSULE_OPEN")
                    .label("胶囊开启")
                    .detail(capsule.getTitle() != null ? capsule.getTitle() : "读到一封写给自己的信")
                    .build());
        }

        events.sort(Comparator
                .comparing(TimelineEventVO::getDate, Comparator.reverseOrder())
                .thenComparingInt(e -> typeOrder(e.getType())));
        return events;
    }

    private Map<Integer, String> moodNameMap() {
        Map<Integer, String> map = new HashMap<>();
        for (Mood mood : moodMapper.selectList(null)) {
            map.put(mood.getId(), mood.getName());
        }
        return map;
    }

    private Map<Long, int[]> taskCounts(List<Long> idealIds) {
        if (idealIds.isEmpty()) {
            return Map.of();
        }
        List<IdealTask> tasks = idealTaskMapper.selectList(new LambdaQueryWrapper<IdealTask>()
                .in(IdealTask::getIdealId, idealIds));
        Map<Long, int[]> map = new HashMap<>();
        for (IdealTask t : tasks) {
            int[] c = map.computeIfAbsent(t.getIdealId(), k -> new int[2]);
            c[0]++;
            if (t.getIsCompleted() != null && t.getIsCompleted() == 1) {
                c[1]++;
            }
        }
        return map;
    }

    private int typeOrder(String type) {
        return switch (type) {
            case "IDEAL_ACTIVE" -> 0;
            case "RECORD" -> 1;
            case "CAPSULE_OPEN" -> 2;
            case "IDEAL_DONE" -> 3;
            default -> 4; // IDEAL_START
        };
    }

    private String truncate(String content) {
        if (content == null) {
            return "";
        }
        String text = content.trim();
        return text.length() <= DETAIL_MAX ? text : text.substring(0, DETAIL_MAX) + "…";
    }
}
