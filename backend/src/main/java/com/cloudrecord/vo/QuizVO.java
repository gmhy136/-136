package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 问答游戏列表项。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizVO {

    private Long id;
    private String title;
    private String description;
    private String status;
    private int questionCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
