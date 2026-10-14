package com.cloudrecord.service.impl;

import com.cloudrecord.common.BizException;
import com.cloudrecord.dto.CreateCapsuleRequest;
import com.cloudrecord.entity.TimeCapsule;
import com.cloudrecord.mapper.TimeCapsuleMapper;
import com.cloudrecord.vo.CapsuleDetailVO;
import com.cloudrecord.vo.CapsuleVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapsuleServiceImplTest {

    @Mock
    private TimeCapsuleMapper capsuleMapper;

    @InjectMocks
    private CapsuleServiceImpl capsuleService;

    private TimeCapsule capsule(Long id, LocalDate openDate, int opened) {
        TimeCapsule c = new TimeCapsule();
        c.setId(id);
        c.setUserId(1L);
        c.setTitle("给未来的自己");
        c.setContent("亲爱的自己，慢慢来。");
        c.setOpenDate(openDate);
        c.setIsOpened(opened);
        c.setRemindSent(0);
        c.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        return c;
    }

    @Test
    void list_computesStates() {
        LocalDate today = LocalDate.now();
        when(capsuleMapper.selectList(any())).thenReturn(List.of(
                capsule(1L, today.plusDays(5), 0),
                capsule(2L, today.minusDays(1), 0),
                capsule(3L, today.minusDays(3), 1)));

        List<CapsuleVO> list = capsuleService.list(1L);

        assertThat(list).hasSize(3);
        assertThat(list.get(0).getState()).isEqualTo("LOCKED");
        assertThat(list.get(0).getDaysLeft()).isEqualTo(5);
        assertThat(list.get(1).getState()).isEqualTo("READY");
        assertThat(list.get(2).getState()).isEqualTo("OPENED");
        assertThat(list.get(2).isOpened()).isTrue();
    }

    @Test
    void create_rejectsTodayOrPast() {
        CreateCapsuleRequest req = new CreateCapsuleRequest();
        req.setContent("你好");
        req.setOpenDate(LocalDate.now());

        assertThatThrownBy(() -> capsuleService.create(1L, req))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(400));

        req.setOpenDate(LocalDate.now().minusDays(1));
        assertThatThrownBy(() -> capsuleService.create(1L, req))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(400));
    }

    @Test
    void create_insertsAndReturnsLocked() {
        doAnswer(inv -> {
            TimeCapsule c = inv.getArgument(0);
            c.setId(10L);
            return 1;
        }).when(capsuleMapper).insert(any(TimeCapsule.class));

        CreateCapsuleRequest req = new CreateCapsuleRequest();
        req.setTitle("  ");
        req.setContent("  你好，未来  ");
        req.setOpenDate(LocalDate.now().plusDays(1));

        CapsuleVO vo = capsuleService.create(1L, req);

        assertThat(vo.getId()).isEqualTo(10L);
        assertThat(vo.getTitle()).isNull();
        assertThat(vo.getState()).isEqualTo("LOCKED");
        assertThat(vo.isOpened()).isFalse();
        verify(capsuleMapper).insert(any(TimeCapsule.class));
    }

    @Test
    void getDetail_locked_returnsNullContent() {
        when(capsuleMapper.selectOne(any())).thenReturn(capsule(1L, LocalDate.now().plusDays(5), 0));

        CapsuleDetailVO vo = capsuleService.getDetail(1L, 1L);

        assertThat(vo.getContent()).isNull();
        assertThat(vo.getState()).isEqualTo("LOCKED");
        assertThat(vo.isOpened()).isFalse();
        verify(capsuleMapper, never()).updateById(any(TimeCapsule.class));
    }

    @Test
    void getDetail_due_marksOpenedAndReturnsContent() {
        when(capsuleMapper.selectOne(any())).thenReturn(capsule(1L, LocalDate.now().minusDays(1), 0));

        CapsuleDetailVO vo = capsuleService.getDetail(1L, 1L);

        assertThat(vo.getContent()).isEqualTo("亲爱的自己，慢慢来。");
        assertThat(vo.getState()).isEqualTo("OPENED");
        assertThat(vo.isOpened()).isTrue();
        verify(capsuleMapper).updateById(any(TimeCapsule.class));
    }

    @Test
    void getDetail_notOwnedThrows404() {
        when(capsuleMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> capsuleService.getDetail(1L, 999L))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(404));
    }

    @Test
    void delete_deletesById() {
        when(capsuleMapper.selectOne(any())).thenReturn(capsule(1L, LocalDate.now().plusDays(5), 0));

        capsuleService.delete(1L, 1L);

        verify(capsuleMapper).deleteById(1L);
    }
}
