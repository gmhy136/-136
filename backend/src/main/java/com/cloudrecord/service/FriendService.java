package com.cloudrecord.service;

import com.cloudrecord.vo.FriendRequestVO;
import com.cloudrecord.vo.FriendRequestsVO;
import com.cloudrecord.vo.FriendVO;
import com.cloudrecord.vo.InviteVO;
import com.cloudrecord.vo.UserVO;

import java.util.List;

/**
 * 好友服务。
 */
public interface FriendService {

    /** 我的好友列表（已接受，双向）。 */
    List<FriendVO> listFriends(Long userId);

    /** 两个用户是否为已接受的好友（双向）。 */
    boolean isFriend(Long userId, Long otherId);

    /** 收到的 + 发出的申请。 */
    FriendRequestsVO listRequests(Long userId);

    /** 通过邮箱发申请。 */
    FriendRequestVO sendRequest(Long userId, String email);

    FriendVO acceptRequest(Long userId, Long requestId);

    void rejectRequest(Long userId, Long requestId);

    void removeFriend(Long userId, Long friendId);

    /** 邮箱搜索用户（用于添加好友）。 */
    UserVO searchUser(Long userId, String email);

    /** 生成邀请链接。 */
    InviteVO createInvite(Long userId);

    /** 点击邀请链接：生成一条「邀请人 → 我」的申请。 */
    void acceptInvite(Long userId, String token);
}
