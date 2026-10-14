package com.cloudrecord.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudrecord.common.Result;
import com.cloudrecord.entity.Mood;
import com.cloudrecord.mapper.MoodMapper;
import com.cloudrecord.vo.MoodVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/moods")
@RequiredArgsConstructor
public class MoodController {

    private final MoodMapper moodMapper;

    @GetMapping
    public Result<List<MoodVO>> list() {
        List<MoodVO> vos = moodMapper.selectList(new LambdaQueryWrapper<Mood>()
                        .orderByAsc(Mood::getSortOrder))
                .stream()
                .map(m -> MoodVO.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .weatherIcon(m.getWeatherIcon())
                        .build())
                .toList();
        return Result.ok(vos);
    }
}
