package com.cloudrecord.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 好友申请汇总（收到的 + 发出的）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestsVO {

    private List<FriendRequestVO> received;
    private List<FriendRequestVO> sent;
}
