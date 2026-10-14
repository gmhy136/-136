package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 记录卡视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordVO {

    private Long id;
    private String content;
    private Integer moodId;
    private String moodName;
    private String moodIcon;
    private String styleKey;
    private String aiType;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
