package com.cloudrecord.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudrecord.entity.TimeCapsule;
import com.cloudrecord.mapper.TimeCapsuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 时间胶囊到期扫描：每日标记到期未开启的胶囊为「已发提醒」。
 * MVP 仅站内提醒（列表「可开启」态 + 读信置 is_opened），不做邮件。
 */
@Component
@RequiredArgsConstructor
public class CapsuleRemindJob {

    private final TimeCapsuleMapper capsuleMapper;

    @Scheduled(cron = "0 7 8 * * *")
    public void scanDueCapsules() {
        LocalDate today = LocalDate.now();
        List<TimeCapsule> due = capsuleMapper.selectList(new LambdaQueryWrapper<TimeCapsule>()
                .le(TimeCapsule::getOpenDate, today)
                .eq(TimeCapsule::getRemindSent, 0)
                .eq(TimeCapsule::getIsOpened, 0));
        for (TimeCapsule capsule : due) {
            capsule.setRemindSent(1);
            capsuleMapper.updateById(capsule);
        }
    }
}
