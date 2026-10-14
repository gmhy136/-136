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
 * AI 今日一句（每人每天缓存一条，降级时也写入固定句）。
 */
@Data
@TableName("daily_quote")
public class DailyQuote {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate quoteDate;

    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
