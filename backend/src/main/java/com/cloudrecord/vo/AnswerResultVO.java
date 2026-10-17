package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 答题结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResultVO {

    private int score;
    private int total;
    private List<AnswerItemResultVO> items;
}
