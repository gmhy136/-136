package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 好友邀请链接视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteVO {

    private String token;
    private String url;
    private LocalDateTime expiresAt;
}
