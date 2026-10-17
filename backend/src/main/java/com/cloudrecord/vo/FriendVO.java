package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 好友视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendVO {

    /** 好友的用户 id。 */
    private Long id;
    private String nickname;
    private String avatarUrl;
    private String bio;

    /** 成为好友时间。 */
    private LocalDateTime since;
}
