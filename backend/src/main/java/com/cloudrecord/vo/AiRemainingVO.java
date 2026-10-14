package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 今日剩余 AI 次数（各场景取最小剩余）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRemainingVO {

    private int limit;
    private int remaining;
}
