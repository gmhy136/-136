package com.cloudrecord.service;

import com.cloudrecord.vo.BadgeVO;

import java.util.List;

/**
 * 亮点报表：成就徽章。
 */
public interface BadgeService {

    /** 返回全部徽章及其解锁状态。 */
    List<BadgeVO> badges(Long userId);
}
