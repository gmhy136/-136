package com.cloudrecord.dto;

import lombok.Data;

/**
 * 选择题选项入参。
 */
@Data
public class OptionInput {

    private String label;

    private Boolean isCorrect;
}
