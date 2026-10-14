package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 时间胶囊列表视图（不含信件内容，锁定时后端强制不可见）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapsuleVO {

    private Long id;
    private String title;
    private LocalDate openDate;
    private boolean isOpened;
    private int daysLeft;
    /** LOCKED 未到期 / READY 到期未开启 / OPENED 已开启。 */
    private String state;
}
