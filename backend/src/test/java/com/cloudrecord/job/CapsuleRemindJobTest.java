package com.cloudrecord.job;

import com.cloudrecord.entity.TimeCapsule;
import com.cloudrecord.mapper.TimeCapsuleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapsuleRemindJobTest {

    @Mock
    private TimeCapsuleMapper capsuleMapper;

    @InjectMocks
    private CapsuleRemindJob remindJob;

    private TimeCapsule capsule(Long id, LocalDate openDate) {
        TimeCapsule c = new TimeCapsule();
        c.setId(id);
        c.setUserId(1L);
        c.setTitle("给未来的自己");
        c.setContent("内容");
        c.setOpenDate(openDate);
        c.setIsOpened(0);
        c.setRemindSent(0);
        return c;
    }

    @Test
    void scanDueCapsules_flipsRemindSent() {
        LocalDate today = LocalDate.now();
        TimeCapsule a = capsule(1L, today.minusDays(1));
        TimeCapsule b = capsule(2L, today);
        when(capsuleMapper.selectList(any())).thenReturn(List.of(a, b));

        remindJob.scanDueCapsules();

        assertThat(a.getRemindSent()).isEqualTo(1);
        assertThat(b.getRemindSent()).isEqualTo(1);
        verify(capsuleMapper, times(2)).updateById(any(TimeCapsule.class));
    }

    @Test
    void scanDueCapsules_nothingDue_noUpdate() {
        when(capsuleMapper.selectList(any())).thenReturn(List.of());

        remindJob.scanDueCapsules();

        verify(capsuleMapper, never()).updateById(any(TimeCapsule.class));
    }
}
