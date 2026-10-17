package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 选项视图（出题人视角，含是否正确）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizOptionVO {

    private Long id;
    private String label;
    private Boolean isCorrect;
}
