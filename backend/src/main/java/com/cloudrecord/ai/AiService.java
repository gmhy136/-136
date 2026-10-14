package com.cloudrecord.ai;

import com.cloudrecord.vo.AiRemainingVO;
import com.cloudrecord.vo.StyleRecommendationVO;

/**
 * AI 能力统一封装：限流、计次、降级（方案设计文档 §7）。
 */
public interface AiService {

    /** AI 扩写（流式）。超限抛 429。 */
    ExpandResult expand(Long userId, String content, Integer moodId);

    /** AI 风格推荐（非流式 JSON）。超限抛 429。 */
    StyleRecommendationVO recommendStyle(Long userId, String content, Integer moodId);

    /** AI 理想拆解（非流式 JSON 数组）。超限抛 429；失败降级为空任务列表。 */
    SplitIdealResult splitIdeal(Long userId, String title, String description);

    /** 今日剩余 AI 次数（各场景取最小剩余，不计次）。 */
    AiRemainingVO remaining(Long userId);
}
