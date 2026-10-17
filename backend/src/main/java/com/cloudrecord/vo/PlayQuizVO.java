package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 答题视角的题目（不含答案）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayQuizVO {

    private Long id;
    private String title;
    private String description;
    private int questionCount;
    private List<PlayQuestionVO> questions;
}
