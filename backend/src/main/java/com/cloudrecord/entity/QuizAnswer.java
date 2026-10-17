package com.cloudrecord.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 答题记录（一次作答）。
 */
@Data
@TableName("quiz_answer")
public class QuizAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long quizId;

    private Long userId;

    /** 1 匿名 / 0 实名。 */
    private Integer anonymous;

    /** 答对题数。 */
    private Integer score;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
