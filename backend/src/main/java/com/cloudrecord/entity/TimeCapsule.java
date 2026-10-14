package com.cloudrecord.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 时间胶囊（写给未来自己的信）。
 */
@Data
@TableName("time_capsule")
public class TimeCapsule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    private LocalDate openDate;

    /** 1 已开启 / 0 未开启。 */
    private Integer isOpened;

    /** 1 已发提醒 / 0 未发。 */
    private Integer remindSent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
