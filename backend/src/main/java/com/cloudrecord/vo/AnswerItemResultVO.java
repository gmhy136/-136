package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单题答题结果（含正确答案）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerItemResultVO {

    private Long questionId;
    private Boolean isCorrect;

    /** 选择题正确选项 id（判断题为 null）。 */
    private Long correctOptionId;

    /** 判断题正确答案（选择题为 null）。 */
    private Boolean correctBool;
}
