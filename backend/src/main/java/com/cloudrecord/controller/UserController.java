package com.cloudrecord.controller;

import com.cloudrecord.common.Result;
import com.cloudrecord.dto.UpdateProfileRequest;
import com.cloudrecord.security.UserContext;
import com.cloudrecord.service.OverviewService;
import com.cloudrecord.service.UserService;
import com.cloudrecord.vo.OverviewVO;
import com.cloudrecord.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OverviewService overviewService;

    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(userService.me(UserContext.requireUserId()));
    }

    @PutMapping("/me")
    public Result<UserVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return Result.ok(userService.updateProfile(UserContext.requireUserId(), request));
    }

    @GetMapping("/me/overview")
    public Result<OverviewVO> overview() {
        return Result.ok(overviewService.overview(UserContext.requireUserId()));
    }
}
