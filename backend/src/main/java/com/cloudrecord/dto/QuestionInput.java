package com.cloudrecord.dto;

import lombok.Data;

import java.util.List;

/**
 * 题目入参（选择题 / 判断题）。
 */
@Data
public class QuestionInput {

    /** CHOICE / JUDGE。 */
    private String type;

    private String content;

    /** 选择题选项。 */
    private List<OptionInput> options;

    /** 判断题答案（true 对 / false 错）。 */
    private Boolean judgeAnswer;
}
