package com.cloudrecord.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudrecord.common.BizException;
import com.cloudrecord.entity.FriendInvite;
import com.cloudrecord.entity.Friendship;
import com.cloudrecord.entity.User;
import com.cloudrecord.mapper.FriendInviteMapper;
import com.cloudrecord.mapper.FriendshipMapper;
import com.cloudrecord.mapper.UserMapper;
import com.cloudrecord.service.FriendService;
import com.cloudrecord.vo.FriendRequestVO;
import com.cloudrecord.vo.FriendRequestsVO;
import com.cloudrecord.vo.FriendVO;
import com.cloudrecord.vo.InviteVO;
import com.cloudrecord.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 好友服务实现。
 */
@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private static final String PENDING = "PENDING";
    private static final String ACCEPTED = "ACCEPTED";
    private static final String REJECTED = "REJECTED";

    private final FriendshipMapper friendshipMapper;
    private final FriendInviteMapper friendInviteMapper;
    private final UserMapper userMapper;

    @Override
    public List<FriendVO> listFriends(Long userId) {
        List<Friendship> rows = friendshipMapper.selectList(new LambdaQueryWrapper<Friendship>()
                .eq(Friendship::getStatus, ACCEPTED)
                .and(w -> w.eq(Friendship::getRequesterId, userId).or().eq(Friendship::getAddresseeId, userId)));
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> friendIds = rows.stream().map(r -> peer(r, userId)).toList();
        Map<Long, User> users = userMapper.selectByIds(friendIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return rows.stream()
                .map(r -> friendVO(users.get(peer(r, userId)), r.getCreatedAt()))
                .toList();
    }

    @Override
    public boolean isFriend(Long userId, Long otherId) {
        Friendship f = findByPair(userId, otherId);
        return f != null && ACCEPTED.equals(f.getStatus());
    }

    @Override
    public FriendRequestsVO listRequests(Long userId) {
        List<Friendship> received = friendshipMapper.selectList(new LambdaQueryWrapper<Friendship>()
                .eq(Friendship::getAddresseeId, userId).eq(Friendship::getStatus, PENDING));
        List<Friendship> sent = friendshipMapper.selectList(new LambdaQueryWrapper<Friendship>()
                .eq(Friendship::getRequesterId, userId).eq(Friendship::getStatus, PENDING));
        return FriendRequestsVO.builder()
                .received(toRequestVOs(received, userId))
                .sent(toRequestVOs(sent, userId))
                .build();
    }

    @Override
    public FriendRequestVO sendRequest(Long userId, String email) {
        User target = requireUserByEmail(email);
        if (target.getId().equals(userId)) {
            throw BizException.badRequest("不能添加自己为好友");
        }
        Friendship f = upsertRequest(userId, target.getId());
        return requestVO(f, target);
    }

    @Override
    public FriendVO acceptRequest(Long userId, Long requestId) {
        Friendship f = requirePendingRequest(userId, requestId);
        f.setStatus(ACCEPTED);
        friendshipMapper.updateById(f);
        User requester = userMapper.selectById(f.getRequesterId());
        return friendVO(requester, f.getCreatedAt());
    }

    @Override
    public void rejectRequest(Long userId, Long requestId) {
        Friendship f = requirePendingRequest(userId, requestId);
        f.setStatus(REJECTED);
        friendshipMapper.updateById(f);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        Friendship f = findByPair(userId, friendId);
        if (f == null || !ACCEPTED.equals(f.getStatus())) {
            throw BizException.notFound("你们还不是好友");
        }
        friendshipMapper.deleteById(f.getId());
    }

    @Override
    public UserVO searchUser(Long userId, String email) {
        return UserVO.from(requireUserByEmail(email));
    }

    @Override
    public InviteVO createInvite(Long userId) {
        FriendInvite invite = new FriendInvite();
        invite.setInviterId(userId);
        invite.setToken(UUID.randomUUID().toString().replace("-", ""));
        invite.setExpiresAt(LocalDateTime.now().plusDays(7));
        friendInviteMapper.insert(invite);
        return InviteVO.builder()
                .token(invite.getToken())
                .url("/invite/" + invite.getToken())
                .expiresAt(invite.getExpiresAt())
                .build();
    }

    @Override
    public void acceptInvite(Long userId, String token) {
        FriendInvite invite = friendInviteMapper.selectOne(new LambdaQueryWrapper<FriendInvite>()
                .eq(FriendInvite::getToken, token));
        if (invite == null) {
            throw BizException.notFound("邀请链接无效");
        }
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw BizException.badRequest("邀请链接已过期");
        }
        if (invite.getInviterId().equals(userId)) {
            throw BizException.badRequest("不能添加自己为好友");
        }
        upsertRequest(invite.getInviterId(), userId);
    }

    // ---- helpers ----

    private User requireUserByEmail(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, normalized));
        if (user == null) {
            throw BizException.notFound("用户不存在");
        }
        return user;
    }

    private Friendship requirePendingRequest(Long userId, Long requestId) {
        Friendship f = friendshipMapper.selectById(requestId);
        if (f == null || !f.getAddresseeId().equals(userId)) {
            throw BizException.notFound("申请不存在");
        }
        if (!PENDING.equals(f.getStatus())) {
            throw BizException.badRequest("该申请已处理");
        }
        return f;
    }

    /** 建立 PENDING 申请：不存在则插入；REJECTED 则删除重建；ACCEPTED/PENDING 报错。 */
    private Friendship upsertRequest(Long requesterId, Long addresseeId) {
        Friendship existing = findByPair(requesterId, addresseeId);
        if (existing != null) {
            if (ACCEPTED.equals(existing.getStatus())) {
                throw BizException.badRequest("你们已经是好友了");
            }
            if (PENDING.equals(existing.getStatus())) {
                throw BizException.badRequest("已存在待处理的好友申请");
            }
            friendshipMapper.deleteById(existing.getId());
        }
        Friendship f = new Friendship();
        f.setRequesterId(requesterId);
        f.setAddresseeId(addresseeId);
        f.setStatus(PENDING);
        friendshipMapper.insert(f);
        return f;
    }

    /** 任意方向的一对用户关系。 */
    private Friendship findByPair(Long a, Long b) {
        return friendshipMapper.selectOne(new LambdaQueryWrapper<Friendship>()
                .and(w -> w
                        .eq(Friendship::getRequesterId, a).eq(Friendship::getAddresseeId, b)
                        .or()
                        .eq(Friendship::getRequesterId, b).eq(Friendship::getAddresseeId, a)));
    }

    private Long peer(Friendship f, Long userId) {
        return f.getRequesterId().equals(userId) ? f.getAddresseeId() : f.getRequesterId();
    }

    private List<FriendRequestVO> toRequestVOs(List<Friendship> rows, Long userId) {
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> peerIds = rows.stream().map(r -> peer(r, userId)).toList();
        Map<Long, User> users = userMapper.selectByIds(peerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return rows.stream().map(r -> requestVO(r, users.get(peer(r, userId)))).toList();
    }

    private FriendRequestVO requestVO(Friendship f, User u) {
        return FriendRequestVO.builder()
                .id(f.getId())
                .userId(u.getId())
                .nickname(u.getNickname())
                .avatarUrl(u.getAvatarUrl())
                .createdAt(f.getCreatedAt())
                .build();
    }

    private FriendVO friendVO(User u, LocalDateTime since) {
        return FriendVO.builder()
                .id(u.getId())
                .nickname(u.getNickname())
                .avatarUrl(u.getAvatarUrl())
                .bio(u.getBio())
                .since(since)
                .build();
    }
}
