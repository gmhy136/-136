package com.cloudrecord.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudrecord.common.BizException;
import com.cloudrecord.dto.CreateCapsuleRequest;
import com.cloudrecord.entity.TimeCapsule;
import com.cloudrecord.mapper.TimeCapsuleMapper;
import com.cloudrecord.service.CapsuleService;
import com.cloudrecord.vo.CapsuleDetailVO;
import com.cloudrecord.vo.CapsuleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 时间胶囊服务实现。
 */
@Service
@RequiredArgsConstructor
public class CapsuleServiceImpl implements CapsuleService {

    private static final String STATE_LOCKED = "LOCKED";
    private static final String STATE_READY = "READY";
    private static final String STATE_OPENED = "OPENED";

    private final TimeCapsuleMapper capsuleMapper;

    @Override
    public List<CapsuleVO> list(Long userId) {
        List<TimeCapsule> capsules = capsuleMapper.selectList(new LambdaQueryWrapper<TimeCapsule>()
                .eq(TimeCapsule::getUserId, userId)
                .orderByAsc(TimeCapsule::getIsOpened)
                .orderByAsc(TimeCapsule::getOpenDate));
        return capsules.stream().map(this::toVO).toList();
    }

    @Override
    public CapsuleVO create(Long userId, CreateCapsuleRequest request) {
        LocalDate openDate = request.getOpenDate();
        if (!openDate.isAfter(LocalDate.now())) {
            throw BizException.badRequest("开启日期最早为明天");
        }
        TimeCapsule capsule = new TimeCapsule();
        capsule.setUserId(userId);
        capsule.setTitle(normalizeTitle(request.getTitle()));
        capsule.setContent(request.getContent().trim());
        capsule.setOpenDate(openDate);
        capsule.setIsOpened(0);
        capsule.setRemindSent(0);
        capsuleMapper.insert(capsule);
        return toVO(capsule);
    }

    @Override
    public CapsuleDetailVO getDetail(Long userId, Long id) {
        TimeCapsule capsule = requireOwned(userId, id);
        boolean opened = isOpened(capsule);
        if (!opened && !capsule.getOpenDate().isAfter(LocalDate.now())) {
            capsule.setIsOpened(1);
            capsuleMapper.updateById(capsule);
            opened = true;
        }
        return toDetailVO(capsule, opened ? capsule.getContent() : null);
    }

    @Override
    public void delete(Long userId, Long id) {
        requireOwned(userId, id);
        capsuleMapper.deleteById(id);
    }

    // ---- helpers ----

    private TimeCapsule requireOwned(Long userId, Long id) {
        TimeCapsule capsule = capsuleMapper.selectOne(new LambdaQueryWrapper<TimeCapsule>()
                .eq(TimeCapsule::getId, id)
                .eq(TimeCapsule::getUserId, userId));
        if (capsule == null) {
            throw BizException.notFound("胶囊不存在");
        }
        return capsule;
    }

    private CapsuleVO toVO(TimeCapsule capsule) {
        return CapsuleVO.builder()
                .id(capsule.getId())
                .title(capsule.getTitle())
                .openDate(capsule.getOpenDate())
                .isOpened(isOpened(capsule))
                .daysLeft(daysLeft(capsule.getOpenDate()))
                .state(state(capsule))
                .build();
    }

    private CapsuleDetailVO toDetailVO(TimeCapsule capsule, String content) {
        return CapsuleDetailVO.builder()
                .id(capsule.getId())
                .title(capsule.getTitle())
                .content(content)
                .openDate(capsule.getOpenDate())
                .isOpened(isOpened(capsule))
                .daysLeft(daysLeft(capsule.getOpenDate()))
                .state(state(capsule))
                .createdAt(capsule.getCreatedAt())
                .build();
    }

    private String state(TimeCapsule capsule) {
        if (isOpened(capsule)) {
            return STATE_OPENED;
        }
        return capsule.getOpenDate().isAfter(LocalDate.now()) ? STATE_LOCKED : STATE_READY;
    }

    private int daysLeft(LocalDate openDate) {
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), openDate);
    }

    private boolean isOpened(TimeCapsule capsule) {
        return capsule.getIsOpened() != null && capsule.getIsOpened() == 1;
    }

    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        return title.trim();
    }
}
