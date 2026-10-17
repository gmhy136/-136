package com.cloudrecord.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 选择题选项。
 */
@Data
@TableName("quiz_option")
public class QuizOption {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;

    private String label;

    /** 1 正确 / 0 错误。 */
    private Integer isCorrect;

    private Integer sortOrder;
}
