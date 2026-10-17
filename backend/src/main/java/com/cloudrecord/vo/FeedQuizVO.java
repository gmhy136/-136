package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 朋友圈题目（好友 + 我的已发布）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedQuizVO {

    private Long id;
    private String title;
    private String description;
    private int questionCount;
    private LocalDateTime publishedAt;

    private Long ownerId;
    private String ownerNickname;
    private String ownerAvatarUrl;

    /** 当前用户是否已作答。 */
    private boolean answered;
}
