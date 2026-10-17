package com.cloudrecord.dto;

import lombok.Data;

/**
 * 单题作答入参。
 */
@Data
public class AnswerInput {

    private Long questionId;

    /** 选择题所选选项 id。 */
    private Long optionId;

    /** 判断题所选（true 对 / false 错）。 */
    private Boolean chosenBool;
}
