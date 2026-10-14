package com.cloudrecord.controller;

import com.cloudrecord.common.Result;
import com.cloudrecord.security.UserContext;
import com.cloudrecord.service.BadgeService;
import com.cloudrecord.vo.BadgeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 亮点报表：成就徽章。
 */
@RestController
@RequestMapping("/api/v1/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping
    public Result<List<BadgeVO>> list() {
        return Result.ok(badgeService.badges(UserContext.requireUserId()));
    }
}
