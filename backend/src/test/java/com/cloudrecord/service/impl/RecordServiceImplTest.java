package com.cloudrecord.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudrecord.common.BizException;
import com.cloudrecord.dto.CreateRecordRequest;
import com.cloudrecord.dto.UpdateRecordRequest;
import com.cloudrecord.entity.CardImage;
import com.cloudrecord.entity.Mood;
import com.cloudrecord.entity.RecordCard;
import com.cloudrecord.mapper.CardImageMapper;
import com.cloudrecord.mapper.MoodMapper;
import com.cloudrecord.mapper.RecordCardMapper;
import com.cloudrecord.vo.PageVO;
import com.cloudrecord.vo.RecordVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordServiceImplTest {

    @Mock
    private RecordCardMapper recordCardMapper;

    @Mock
    private CardImageMapper cardImageMapper;

    @Mock
    private MoodMapper moodMapper;

    @InjectMocks
    private RecordServiceImpl recordService;

    private RecordCard card(Long id) {
        RecordCard c = new RecordCard();
        c.setId(id);
        c.setUserId(1L);
        c.setContent("今天天气不错");
        c.setMoodId(1);
        c.setStyleKey("warm-sunny");
        c.setAiType("原创");
        c.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        c.setUpdatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        return c;
    }

    private Mood mood() {
        Mood m = new Mood();
        m.setId(1);
        m.setName("开心");
        m.setWeatherIcon("md-sunny");
        return m;
    }

    private CardImage image(Long cardId, String url, int sort) {
        CardImage img = new CardImage();
        img.setCardId(cardId);
        img.setUrl(url);
        img.setSortOrder(sort);
        return img;
    }

    @Test
    void list_assemblesMoodAndImages() {
        Page<RecordCard> page = new Page<>(1, 10);
        page.setRecords(List.of(card(1L)));
        page.setTotal(1);
        when(recordCardMapper.selectPage(any(), any())).thenReturn(page);
        when(moodMapper.selectList(any())).thenReturn(List.of(mood()));
        when(cardImageMapper.selectList(any())).thenReturn(List.of(image(1L, "/uploads/a.png", 0)));

        PageVO<RecordVO> vo = recordService.list(1L, 1, 10);

        assertThat(vo.getTotal()).isEqualTo(1);
        RecordVO r = vo.getRecords().get(0);
        assertThat(r.getMoodName()).isEqualTo("开心");
        assertThat(r.getMoodIcon()).isEqualTo("md-sunny");
        assertThat(r.getImageUrls()).containsExactly("/uploads/a.png");
    }

    @Test
    void get_ownedReturnsVO() {
        when(recordCardMapper.selectOne(any())).thenReturn(card(1L));
        when(moodMapper.selectById(1)).thenReturn(mood());
        when(cardImageMapper.selectList(any())).thenReturn(List.of(image(1L, "/uploads/a.png", 0)));

        RecordVO vo = recordService.get(1L, 1L);

        assertThat(vo.getContent()).isEqualTo("今天天气不错");
        assertThat(vo.getMoodName()).isEqualTo("开心");
        assertThat(vo.getImageUrls()).containsExactly("/uploads/a.png");
    }

    @Test
    void get_notOwnedThrows404() {
        when(recordCardMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> recordService.get(1L, 999L))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(404));
    }

    @Test
    void create_success() {
        when(moodMapper.selectById(1)).thenReturn(mood());
        doAnswer(inv -> {
            RecordCard c = inv.getArgument(0);
            c.setId(10L);
            return 1;
        }).when(recordCardMapper).insert(any(RecordCard.class));

        CreateRecordRequest req = new CreateRecordRequest();
        req.setContent("今天");
        req.setMoodId(1);
        req.setImageUrls(List.of("/uploads/a.png"));

        RecordVO vo = recordService.create(1L, req);

        assertThat(vo.getId()).isEqualTo(10L);
        assertThat(vo.getMoodName()).isEqualTo("开心");
        verify(cardImageMapper).insert(any(CardImage.class));
    }

    @Test
    void create_tooManyImagesThrows400() {
        CreateRecordRequest req = new CreateRecordRequest();
        req.setContent("x");
        req.setImageUrls(List.of("a", "b", "c", "d"));

        assertThatThrownBy(() -> recordService.create(1L, req))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(400));
    }

    @Test
    void create_invalidMoodThrows400() {
        when(moodMapper.selectById(99)).thenReturn(null);
        CreateRecordRequest req = new CreateRecordRequest();
        req.setContent("x");
        req.setMoodId(99);

        assertThatThrownBy(() -> recordService.create(1L, req))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(400));
    }

    @Test
    void update_replacesImages() {
        when(recordCardMapper.selectOne(any())).thenReturn(card(1L));
        when(moodMapper.selectById(1)).thenReturn(mood());

        UpdateRecordRequest req = new UpdateRecordRequest();
        req.setContent("改");
        req.setMoodId(1);
        req.setImageUrls(List.of("/uploads/new.png"));

        RecordVO vo = recordService.update(1L, 1L, req);

        assertThat(vo.getContent()).isEqualTo("改");
        verify(cardImageMapper).delete(any());
        verify(cardImageMapper).insert(any(CardImage.class));
    }

    @Test
    void delete_logicDelete() {
        when(recordCardMapper.selectOne(any())).thenReturn(card(1L));

        recordService.delete(1L, 1L);

        verify(recordCardMapper).deleteById(1L);
    }
}
