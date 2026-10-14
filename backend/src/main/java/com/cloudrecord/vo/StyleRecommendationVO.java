package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 风格推荐结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleRecommendationVO {

    /** 卡片风格 key（warm-sunny 等，见方案设计文档 §14.3）。 */
    private String styleKey;

    /** 推荐理由（5~15 字）。 */
    private String reason;

    /** 该场景今日剩余次数。 */
    private int remaining;
}
