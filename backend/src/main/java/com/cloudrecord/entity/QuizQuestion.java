package com.cloudrecord.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 问答题目。
 */
@Data
@TableName("quiz_question")
public class QuizQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long quizId;

    /** CHOICE / JUDGE。 */
    private String type;

    private String content;

    /** 判断题答案 1 对 / 0 错；选择题为 null。 */
    private Integer judgeAnswer;

    private Integer sortOrder;
}
