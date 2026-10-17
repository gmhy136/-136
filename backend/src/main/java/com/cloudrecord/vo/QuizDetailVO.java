package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问答游戏详情（出题人视角，含答案）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizDetailVO {

    private Long id;
    private String title;
    private String description;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private List<QuizQuestionVO> questions;
}
