package com.cloudrecord.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudrecord.common.BizException;
import com.cloudrecord.dto.AddTaskRequest;
import com.cloudrecord.dto.CreateIdealRequest;
import com.cloudrecord.entity.CheckIn;
import com.cloudrecord.entity.Ideal;
import com.cloudrecord.entity.IdealTask;
import com.cloudrecord.mapper.CheckInMapper;
import com.cloudrecord.mapper.IdealMapper;
import com.cloudrecord.mapper.IdealTaskMapper;
import com.cloudrecord.service.IdealService;
import com.cloudrecord.vo.CalendarVO;
import com.cloudrecord.vo.IdealDetailVO;
import com.cloudrecord.vo.IdealTaskVO;
import com.cloudrecord.vo.IdealVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 理想卡服务实现。
 */
@Service
@RequiredArgsConstructor
public class IdealServiceImpl implements IdealService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_DONE = "DONE";
    private static final String VISIBILITY_PRIVATE = "PRIVATE";
    private static final String VISIBILITY_PUBLIC = "PUBLIC";

    private final IdealMapper idealMapper;
    private final IdealTaskMapper idealTaskMapper;
    private final CheckInMapper checkInMapper;

    @Override
    public List<IdealVO> list(Long userId) {
        List<Ideal> ideals = idealMapper.selectList(new LambdaQueryWrapper<Ideal>()
                .eq(Ideal::getUserId, userId)
                .orderByAsc(Ideal::getStatus)
                .orderByDesc(Ideal::getCreatedAt));
        if (ideals.isEmpty()) {
            return List.of();
        }
        List<Long> ids = ideals.stream().map(Ideal::getId).toList();
        Map<Long, int[]> counts = taskCounts(ids);
        return ideals.stream().map(i -> {
            int[] c = counts.getOrDefault(i.getId(), new int[2]);
            return toVO(i, c[0], c[1]);
        }).toList();
    }

    @Override
    public IdealVO create(Long userId, CreateIdealRequest request) {
        Ideal ideal = new Ideal();
        ideal.setUserId(userId);
        ideal.setTitle(request.getTitle().trim());
        ideal.setDescription(request.getDescription());
        ideal.setVisibility(normalizeVisibility(request.getVisibility()));
        ideal.setStatus(STATUS_ACTIVE);
        ideal.setTargetDate(request.getTargetDate());
        idealMapper.insert(ideal);
        return toVO(ideal, 0, 0);
    }

    @Override
    public IdealDetailVO getDetail(Long userId, Long id) {
        Ideal ideal = requireOwned(userId, id);
        return buildDetail(ideal);
    }

    @Override
    public void delete(Long userId, Long id) {
        requireOwned(userId, id);
        idealMapper.deleteById(id);
    }

    @Override
    public IdealTaskVO addTask(Long userId, Long idealId, AddTaskRequest request) {
        requireOwned(userId, idealId);
        long count = idealTaskMapper.selectCount(new LambdaQueryWrapper<IdealTask>()
                .eq(IdealTask::getIdealId, idealId));
        IdealTask task = new IdealTask();
        task.setIdealId(idealId);
        task.setContent(request.getContent().trim());
        task.setIsCompleted(0);
        task.setSortOrder((int) count);
        idealTaskMapper.insert(task);
        syncStatus(idealId);
        return toTaskVO(task);
    }

    @Override
    public IdealTaskVO toggleTask(Long userId, Long idealId, Long taskId) {
        requireOwned(userId, idealId);
        IdealTask task = idealTaskMapper.selectById(taskId);
        if (task == null || !idealId.equals(task.getIdealId())) {
            throw BizException.notFound("任务不存在");
        }
        task.setIsCompleted(isDone(task) ? 0 : 1);
        idealTaskMapper.updateById(task);
        syncStatus(idealId);
        return toTaskVO(task);
    }

    @Override
    public IdealDetailVO checkIn(Long userId, Long idealId) {
        Ideal ideal = requireOwned(userId, idealId);
        LocalDate today = LocalDate.now();
        CheckIn existing = checkInMapper.selectOne(new LambdaQueryWrapper<CheckIn>()
                .eq(CheckIn::getIdealId, idealId)
                .eq(CheckIn::getCheckDate, today));
        if (existing == null) {
            CheckIn c = new CheckIn();
            c.setIdealId(idealId);
            c.setUserId(userId);
            c.setCheckDate(today);
            c.setStatus(1);
            try {
                checkInMapper.insert(c);
            } catch (DuplicateKeyException e) {
                // 并发下唯一键兜底，已存在视为已打卡
            }
        }
        return buildDetail(ideal);
    }

    @Override
    public CalendarVO calendar(Long userId, Long idealId, String month) {
        requireOwned(userId, idealId);
        YearMonth ym = parseMonth(month);
        List<Integer> days = checkInMapper.selectList(new LambdaQueryWrapper<CheckIn>()
                        .eq(CheckIn::getIdealId, idealId)
                        .ge(CheckIn::getCheckDate, ym.atDay(1))
                        .le(CheckIn::getCheckDate, ym.atEndOfMonth()))
                .stream()
                .map(c -> c.getCheckDate().getDayOfMonth())
                .toList();
        List<LocalDate> all = checkInDates(idealId);
        return CalendarVO.builder()
                .month(ym.toString())
                .days(days)
                .streakDays(streak(all))
                .monthCount(days.size())
                .build();
    }

    // ---- helpers ----

    private IdealDetailVO buildDetail(Ideal ideal) {
        List<IdealTask> tasks = idealTaskMapper.selectList(new LambdaQueryWrapper<IdealTask>()
                .eq(IdealTask::getIdealId, ideal.getId())
                .orderByAsc(IdealTask::getSortOrder));
        List<LocalDate> dates = checkInDates(ideal.getId());
        LocalDate today = LocalDate.now();
        int completed = (int) tasks.stream().filter(this::isDone).count();
        return IdealDetailVO.builder()
                .id(ideal.getId())
                .title(ideal.getTitle())
                .description(ideal.getDescription())
                .visibility(ideal.getVisibility())
                .status(ideal.getStatus())
                .targetDate(ideal.getTargetDate())
                .taskCount(tasks.size())
                .completedTaskCount(completed)
                .tasks(tasks.stream().map(this::toTaskVO).toList())
                .todayChecked(dates.contains(today))
                .streakDays(streak(dates))
                .monthCheckInDays((int) dates.stream().filter(d -> isSameMonth(d, today)).count())
                .checkInDates(dates)
                .build();
    }

    private Ideal requireOwned(Long userId, Long id) {
        Ideal ideal = idealMapper.selectOne(new LambdaQueryWrapper<Ideal>()
                .eq(Ideal::getId, id)
                .eq(Ideal::getUserId, userId));
        if (ideal == null) {
            throw BizException.notFound("理想不存在");
        }
        return ideal;
    }

    private Map<Long, int[]> taskCounts(List<Long> idealIds) {
        List<IdealTask> tasks = idealTaskMapper.selectList(new LambdaQueryWrapper<IdealTask>()
                .in(IdealTask::getIdealId, idealIds));
        Map<Long, int[]> map = new HashMap<>();
        for (IdealTask t : tasks) {
            int[] c = map.computeIfAbsent(t.getIdealId(), k -> new int[2]);
            c[0]++;
            if (isDone(t)) {
                c[1]++;
            }
        }
        return map;
    }

    private void syncStatus(Long idealId) {
        Ideal ideal = idealMapper.selectById(idealId);
        if (ideal == null) {
            return;
        }
        List<IdealTask> tasks = idealTaskMapper.selectList(new LambdaQueryWrapper<IdealTask>()
                .eq(IdealTask::getIdealId, idealId));
        boolean allDone = !tasks.isEmpty() && tasks.stream().allMatch(this::isDone);
        String status = allDone ? STATUS_DONE : STATUS_ACTIVE;
        if (!status.equals(ideal.getStatus())) {
            ideal.setStatus(status);
            idealMapper.updateById(ideal);
        }
    }

    private List<LocalDate> checkInDates(Long idealId) {
        return checkInMapper.selectList(new LambdaQueryWrapper<CheckIn>()
                        .eq(CheckIn::getIdealId, idealId)
                        .orderByAsc(CheckIn::getCheckDate))
                .stream()
                .map(CheckIn::getCheckDate)
                .distinct()
                .toList();
    }

    private int streak(List<LocalDate> dates) {
        if (dates.isEmpty()) {
            return 0;
        }
        Set<LocalDate> set = new HashSet<>(dates);
        LocalDate cursor = LocalDate.now();
        if (!set.contains(cursor)) {
            cursor = cursor.minusDays(1);
        }
        int count = 0;
        while (set.contains(cursor)) {
            count++;
            cursor = cursor.minusDays(1);
        }
        return count;
    }

    private boolean isDone(IdealTask task) {
        return task.getIsCompleted() != null && task.getIsCompleted() == 1;
    }

    private boolean isSameMonth(LocalDate a, LocalDate b) {
        return a.getYear() == b.getYear() && a.getMonth() == b.getMonth();
    }

    private YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month);
        } catch (Exception e) {
            return YearMonth.now();
        }
    }

    private String normalizeVisibility(String visibility) {
        String v = visibility == null ? "" : visibility.trim();
        return VISIBILITY_PUBLIC.equalsIgnoreCase(v) ? VISIBILITY_PUBLIC : VISIBILITY_PRIVATE;
    }

    private IdealVO toVO(Ideal ideal, int total, int completed) {
        return IdealVO.builder()
                .id(ideal.getId())
                .title(ideal.getTitle())
                .description(ideal.getDescription())
                .visibility(ideal.getVisibility())
                .status(ideal.getStatus())
                .targetDate(ideal.getTargetDate())
                .taskCount(total)
                .completedTaskCount(completed)
                .build();
    }

    private IdealTaskVO toTaskVO(IdealTask task) {
        return IdealTaskVO.builder()
                .id(task.getId())
                .content(task.getContent())
                .completed(isDone(task))
                .sortOrder(task.getSortOrder() == null ? 0 : task.getSortOrder())
                .build();
    }
}
