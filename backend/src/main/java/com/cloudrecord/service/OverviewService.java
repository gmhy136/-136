package com.cloudrecord.service;

import com.cloudrecord.vo.OverviewVO;

/**
 * 个人主页聚合：用户信息 + 卡片/理想/胶囊 + 统计。
 */
public interface OverviewService {

    OverviewVO overview(Long userId);
}
