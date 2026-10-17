package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 题目视图（出题人视角，含答案）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionVO {

    private Long id;
    private String type;
    private String content;

    /** 判断题答案（选择题为 null）。 */
    private Boolean judgeAnswer;

    private List<QuizOptionVO> options;
}
