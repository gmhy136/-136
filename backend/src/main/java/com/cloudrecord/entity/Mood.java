package com.cloudrecord.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 心情字典实体（6 条种子数据）。
 */
@Data
@TableName("mood")
public class Mood {

    @TableId(type = IdType.INPUT)
    private Integer id;

    private String name;

    private String emoji;

    /** 心情点样式（md-sunny 等）。 */
    private String weatherIcon;

    private Integer sortOrder;
}
