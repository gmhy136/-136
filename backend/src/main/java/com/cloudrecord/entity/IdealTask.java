package com.cloudrecord.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 理想任务（AI 拆解 / 手动添加）。
 */
@Data
@TableName("ideal_task")
public class IdealTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long idealId;

    private String content;

    /** 1 完成 / 0 未完成。 */
    private Integer isCompleted;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
