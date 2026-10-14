package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 时间胶囊详情视图（未到期时 content 为 null，开启后可见全文）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapsuleDetailVO {

    private Long id;
    private String title;
    private String content;
    private LocalDate openDate;
    private boolean isOpened;
    private int daysLeft;
    private String state;
    private LocalDateTime createdAt;
}
