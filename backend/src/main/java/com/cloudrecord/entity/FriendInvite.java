package com.cloudrecord.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友邀请链接（7 天有效，可复用）。
 */
@Data
@TableName("friend_invite")
public class FriendInvite {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long inviterId;

    private String token;

    private LocalDateTime expiresAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
