package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 心情字典视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoodVO {

    private Integer id;
    private String name;
    private String weatherIcon;
}
