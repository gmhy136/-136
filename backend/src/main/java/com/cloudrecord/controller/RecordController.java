package com.cloudrecord.controller;

import com.cloudrecord.common.Result;
import com.cloudrecord.dto.CreateRecordRequest;
import com.cloudrecord.dto.UpdateRecordRequest;
import com.cloudrecord.security.UserContext;
import com.cloudrecord.service.RecordService;
import com.cloudrecord.vo.PageVO;
import com.cloudrecord.vo.RecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @GetMapping
    public Result<PageVO<RecordVO>> list(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "10") long size) {
        return Result.ok(recordService.list(UserContext.requireUserId(), page, size));
    }

    @PostMapping
    public Result<RecordVO> create(@Valid @RequestBody CreateRecordRequest request) {
        return Result.ok(recordService.create(UserContext.requireUserId(), request));
    }

    @GetMapping("/{id}")
    public Result<RecordVO> get(@PathVariable Long id) {
        return Result.ok(recordService.get(UserContext.requireUserId(), id));
    }

    @PutMapping("/{id}")
    public Result<RecordVO> update(@PathVariable Long id, @Valid @RequestBody UpdateRecordRequest request) {
        return Result.ok(recordService.update(UserContext.requireUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        recordService.delete(UserContext.requireUserId(), id);
        return Result.ok();
    }
}
