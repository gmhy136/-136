package com.cloudrecord.controller;

import com.cloudrecord.common.Result;
import com.cloudrecord.dto.AddTaskRequest;
import com.cloudrecord.dto.CreateIdealRequest;
import com.cloudrecord.security.UserContext;
import com.cloudrecord.service.IdealService;
import com.cloudrecord.vo.CalendarVO;
import com.cloudrecord.vo.IdealDetailVO;
import com.cloudrecord.vo.IdealTaskVO;
import com.cloudrecord.vo.IdealVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ideals")
@RequiredArgsConstructor
public class IdealController {

    private final IdealService idealService;

    @GetMapping
    public Result<List<IdealVO>> list() {
        return Result.ok(idealService.list(UserContext.requireUserId()));
    }

    @PostMapping
    public Result<IdealVO> create(@Valid @RequestBody CreateIdealRequest request) {
        return Result.ok(idealService.create(UserContext.requireUserId(), request));
    }

    @GetMapping("/{id}")
    public Result<IdealDetailVO> get(@PathVariable Long id) {
        return Result.ok(idealService.getDetail(UserContext.requireUserId(), id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        idealService.delete(UserContext.requireUserId(), id);
        return Result.ok();
    }

    @PostMapping("/{id}/tasks")
    public Result<IdealTaskVO> addTask(@PathVariable Long id, @Valid @RequestBody AddTaskRequest request) {
        return Result.ok(idealService.addTask(UserContext.requireUserId(), id, request));
    }

    @PostMapping("/{id}/tasks/{taskId}/toggle")
    public Result<IdealTaskVO> toggleTask(@PathVariable Long id, @PathVariable Long taskId) {
        return Result.ok(idealService.toggleTask(UserContext.requireUserId(), id, taskId));
    }

    @PostMapping("/{id}/checkin")
    public Result<IdealDetailVO> checkIn(@PathVariable Long id) {
        return Result.ok(idealService.checkIn(UserContext.requireUserId(), id));
    }

    @GetMapping("/{id}/calendar")
    public Result<CalendarVO> calendar(@PathVariable Long id, @RequestParam(required = false) String month) {
        return Result.ok(idealService.calendar(UserContext.requireUserId(), id, month));
    }
}
