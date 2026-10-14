package com.cloudrecord.ai;

import com.cloudrecord.common.BizException;
import com.cloudrecord.entity.AiUsageLog;
import com.cloudrecord.entity.Mood;
import com.cloudrecord.mapper.AiUsageLogMapper;
import com.cloudrecord.mapper.MoodMapper;
import com.cloudrecord.vo.AiRemainingVO;
import com.cloudrecord.vo.StyleRecommendationVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiServiceImplTest {

    @Mock
    private DeepSeekClient deepSeekClient;

    @Mock
    private AiUsageLogMapper aiUsageLogMapper;

    @Mock
    private MoodMapper moodMapper;

    private AiServiceImpl aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiServiceImpl(deepSeekClient, aiUsageLogMapper, moodMapper, new ObjectMapper());
    }

    private Mood mood(String name) {
        Mood m = new Mood();
        m.setId(1);
        m.setName(name);
        return m;
    }

    @Test
    void expand_underLimit_streamsAndCounts() {
        when(aiUsageLogMapper.selectCount(any())).thenReturn(0L);
        when(moodMapper.selectById(1)).thenReturn(mood("开心"));
        when(deepSeekClient.stream(anyString(), anyString(), any())).thenReturn(Flux.just("今", "天"));

        ExpandResult result = aiService.expand(1L, "今天", 1);

        assertThat(result.remaining()).isEqualTo(19);
        assertThat(result.deltas().collectList().block()).containsExactly("今", "天");
        verify(aiUsageLogMapper).insert(any(AiUsageLog.class));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(deepSeekClient).stream(anyString(), captor.capture(), any());
        assertThat(captor.getValue()).contains("今天").contains("开心");
    }

    @Test
    void expand_overLimit_throws429() {
        when(aiUsageLogMapper.selectCount(any())).thenReturn(20L);

        assertThatThrownBy(() -> aiService.expand(1L, "今天", 1))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(429));
    }

    @Test
    void expand_nullMood_omitsMoodClause() {
        when(aiUsageLogMapper.selectCount(any())).thenReturn(0L);
        when(deepSeekClient.stream(anyString(), anyString(), any())).thenReturn(Flux.empty());

        aiService.expand(1L, "今天", null);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(deepSeekClient).stream(anyString(), captor.capture(), any());
        assertThat(captor.getValue()).contains("今天").doesNotContain("心情");
    }

    @Test
    void recommendStyle_validJson_parses() {
        when(aiUsageLogMapper.selectCount(any())).thenReturn(0L);
        when(deepSeekClient.call(anyString(), anyString(), any()))
                .thenReturn("{\"style_key\":\"quiet-blue\",\"reason\":\"安静的日子\"}");

        StyleRecommendationVO vo = aiService.recommendStyle(1L, "今天", 1);

        assertThat(vo.getStyleKey()).isEqualTo("quiet-blue");
        assertThat(vo.getReason()).isEqualTo("安静的日子");
        assertThat(vo.getRemaining()).isEqualTo(19);
        verify(aiUsageLogMapper).insert(any(AiUsageLog.class));
    }

    @Test
    void recommendStyle_invalidJson_fallsBackToDefault() {
        when(aiUsageLogMapper.selectCount(any())).thenReturn(0L);
        when(deepSeekClient.call(anyString(), anyString(), any())).thenReturn("随便说点什么");

        StyleRecommendationVO vo = aiService.recommendStyle(1L, "今天", null);

        assertThat(vo.getStyleKey()).isEqualTo("paper-white");
    }

    @Test
    void recommendStyle_unknownKey_fallsBackToDefault() {
        when(aiUsageLogMapper.selectCount(any())).thenReturn(0L);
        when(deepSeekClient.call(anyString(), anyString(), any()))
                .thenReturn("{\"style_key\":\"neon-green\",\"reason\":\"x\"}");

        StyleRecommendationVO vo = aiService.recommendStyle(1L, "今天", null);

        assertThat(vo.getStyleKey()).isEqualTo("paper-white");
    }

    @Test
    void recommendStyle_overLimit_throws429() {
        when(aiUsageLogMapper.selectCount(any())).thenReturn(20L);

        assertThatThrownBy(() -> aiService.recommendStyle(1L, "今天", 1))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(429));
    }

    @Test
    void recommendStyle_deepSeekFails_fallsBack() {
        when(aiUsageLogMapper.selectCount(any())).thenReturn(0L);
        when(deepSeekClient.call(anyString(), anyString(), any())).thenThrow(new RuntimeException("boom"));

        StyleRecommendationVO vo = aiService.recommendStyle(1L, "今天", null);

        assertThat(vo.getStyleKey()).isEqualTo("paper-white");
    }

    @Test
    void splitIdeal_validArray_parsesTasks() {
        when(aiUsageLogMapper.selectCount(any())).thenReturn(0L);
        when(deepSeekClient.call(anyString(), anyString(), any()))
                .thenReturn("[{\"content\":\"认识五线谱\"},{\"content\":\"练熟右手\"}]");

        SplitIdealResult result = aiService.splitIdeal(1L, "学会弹歌", "描述");

        assertThat(result.remaining()).isEqualTo(19);
        assertThat(result.tasks()).containsExactly("认识五线谱", "练熟右手");
        verify(aiUsageLogMapper).insert(any(AiUsageLog.class));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(deepSeekClient).call(anyString(), captor.capture(), any());
        assertThat(captor.getValue()).contains("学会弹歌").contains("描述");
    }

    @Test
    void splitIdeal_invalidJson_returnsEmpty() {
        when(aiUsageLogMapper.selectCount(any())).thenReturn(0L);
        when(deepSeekClient.call(anyString(), anyString(), any())).thenReturn("拆解如下：1...");

        SplitIdealResult result = aiService.splitIdeal(1L, "学会弹歌", null);

        assertThat(result.tasks()).isEmpty();
    }

    @Test
    void splitIdeal_overLimit_throws429() {
        when(aiUsageLogMapper.selectCount(any())).thenReturn(20L);

        assertThatThrownBy(() -> aiService.splitIdeal(1L, "学会弹歌", null))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(429));
    }

    @Test
    void splitIdeal_deepSeekFails_returnsEmpty() {
        when(aiUsageLogMapper.selectCount(any())).thenReturn(0L);
        when(deepSeekClient.call(anyString(), anyString(), any())).thenThrow(new RuntimeException("boom"));

        SplitIdealResult result = aiService.splitIdeal(1L, "学会弹歌", null);

        assertThat(result.tasks()).isEmpty();
    }

    @Test
    void remaining_returnsMinAcrossScenes() {
        when(aiUsageLogMapper.selectCount(any())).thenReturn(5L, 10L, 15L);

        AiRemainingVO vo = aiService.remaining(1L);

        assertThat(vo.getLimit()).isEqualTo(20);
        assertThat(vo.getRemaining()).isEqualTo(5);
        verify(aiUsageLogMapper, times(3)).selectCount(any());
    }

    @Test
    void remaining_fullUsage_returnsZero() {
        when(aiUsageLogMapper.selectCount(any())).thenReturn(20L);

        AiRemainingVO vo = aiService.remaining(1L);

        assertThat(vo.getRemaining()).isZero();
    }
}
