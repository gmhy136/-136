package com.cloudrecord.controller;

import com.cloudrecord.common.Result;
import com.cloudrecord.dto.CreateCapsuleRequest;
import com.cloudrecord.security.UserContext;
import com.cloudrecord.service.CapsuleService;
import com.cloudrecord.vo.CapsuleDetailVO;
import com.cloudrecord.vo.CapsuleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/capsules")
@RequiredArgsConstructor
public class CapsuleController {

    private final CapsuleService capsuleService;

    @GetMapping
    public Result<List<CapsuleVO>> list() {
        return Result.ok(capsuleService.list(UserContext.requireUserId()));
    }

    @PostMapping
    public Result<CapsuleVO> create(@Valid @RequestBody CreateCapsuleRequest request) {
        return Result.ok(capsuleService.create(UserContext.requireUserId(), request));
    }

    @GetMapping("/{id}")
    public Result<CapsuleDetailVO> get(@PathVariable Long id) {
        return Result.ok(capsuleService.getDetail(UserContext.requireUserId(), id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        capsuleService.delete(UserContext.requireUserId(), id);
        return Result.ok();
    }
}
