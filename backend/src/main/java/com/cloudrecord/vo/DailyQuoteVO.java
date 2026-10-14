package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 今日一句（每人每天一条，按日缓存）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyQuoteVO {

    private String content;
}
