package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 答题视角的选项（不含是否正确）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayOptionVO {

    private Long id;
    private String label;
}
