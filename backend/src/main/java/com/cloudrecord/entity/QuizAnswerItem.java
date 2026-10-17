package com.cloudrecord.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 答题明细（每题一条）。
 */
@Data
@TableName("quiz_answer_item")
public class QuizAnswerItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long answerId;

    private Long questionId;

    /** 选择题所选选项。 */
    private Long chosenOptionId;

    /** 判断题所选 1 / 0。 */
    private Integer chosenBool;

    /** 1 答对 / 0 答错。 */
    private Integer isCorrect;
}
