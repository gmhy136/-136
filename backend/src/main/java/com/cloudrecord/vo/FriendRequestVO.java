package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 好友申请视图（收到的申请：userId 为申请人；发出的申请：userId 为接收人）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestVO {

    /** friendship 记录 id。 */
    private Long id;

    /** 对方用户 id。 */
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
