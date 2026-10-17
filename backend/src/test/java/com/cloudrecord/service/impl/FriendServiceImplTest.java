package com.cloudrecord.service.impl;

import com.cloudrecord.common.BizException;
import com.cloudrecord.entity.FriendInvite;
import com.cloudrecord.entity.Friendship;
import com.cloudrecord.entity.User;
import com.cloudrecord.mapper.FriendInviteMapper;
import com.cloudrecord.mapper.FriendshipMapper;
import com.cloudrecord.mapper.UserMapper;
import com.cloudrecord.vo.FriendRequestVO;
import com.cloudrecord.vo.FriendRequestsVO;
import com.cloudrecord.vo.FriendVO;
import com.cloudrecord.vo.InviteVO;
import com.cloudrecord.vo.UserVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendServiceImplTest {

    @Mock
    private FriendshipMapper friendshipMapper;

    @Mock
    private FriendInviteMapper friendInviteMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private FriendServiceImpl friendService;

    // ---- helpers ----

    private User user(Long id, String email, String nickname) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setNickname(nickname);
        u.setAvatarUrl("http://a/" + id + ".png");
        u.setBio("bio" + id);
        return u;
    }

    private Friendship friendship(Long id, Long requester, Long addressee, String status) {
        Friendship f = new Friendship();
        f.setId(id);
        f.setRequesterId(requester);
        f.setAddresseeId(addressee);
        f.setStatus(status);
        f.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        return f;
    }

    private FriendInvite invite(Long inviter, String token, LocalDateTime expiresAt) {
        FriendInvite i = new FriendInvite();
        i.setId(1L);
        i.setInviterId(inviter);
        i.setToken(token);
        i.setExpiresAt(expiresAt);
        return i;
    }

    // ---- sendRequest ----

    @Test
    void sendRequest_createsPending() {
        when(userMapper.selectOne(any())).thenReturn(user(2L, "b@test.com", "B"));
        when(friendshipMapper.selectOne(any())).thenReturn(null);
        doAnswer(inv -> {
            Friendship f = inv.getArgument(0);
            f.setId(10L);
            return 1;
        }).when(friendshipMapper).insert(any(Friendship.class));

        FriendRequestVO vo = friendService.sendRequest(1L, "  B@test.com ");

        assertThat(vo.getId()).isEqualTo(10L);
        assertThat(vo.getUserId()).isEqualTo(2L);
        assertThat(vo.getNickname()).isEqualTo("B");
        verify(friendshipMapper).insert(any(Friendship.class));
    }

    @Test
    void sendRequest_targetNotFoundThrows404() {
        when(userMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> friendService.sendRequest(1L, "nobody@test.com"))
                .isInstanceOfSatisfying(BizException.class, e -> assertThat(e.getCode()).isEqualTo(404));
    }

    @Test
    void sendRequest_selfThrows400() {
        when(userMapper.selectOne(any())).thenReturn(user(1L, "a@test.com", "A"));

        assertThatThrownBy(() -> friendService.sendRequest(1L, "a@test.com"))
                .isInstanceOfSatisfying(BizException.class, e -> assertThat(e.getCode()).isEqualTo(400));
    }

    @Test
    void sendRequest_alreadyFriendThrows400() {
        when(userMapper.selectOne(any())).thenReturn(user(2L, "b@test.com", "B"));
        when(friendshipMapper.selectOne(any())).thenReturn(friendship(5L, 2L, 1L, "ACCEPTED"));

        assertThatThrownBy(() -> friendService.sendRequest(1L, "b@test.com"))
                .isInstanceOfSatisfying(BizException.class, e -> assertThat(e.getCode()).isEqualTo(400));
        verify(friendshipMapper, never()).insert(any(Friendship.class));
    }

    @Test
    void sendRequest_alreadyPendingThrows400() {
        when(userMapper.selectOne(any())).thenReturn(user(2L, "b@test.com", "B"));
        when(friendshipMapper.selectOne(any())).thenReturn(friendship(5L, 2L, 1L, "PENDING"));

        assertThatThrownBy(() -> friendService.sendRequest(1L, "b@test.com"))
                .isInstanceOfSatisfying(BizException.class, e -> assertThat(e.getCode()).isEqualTo(400));
    }

    @Test
    void sendRequest_rejectedResetsToPending() {
        when(userMapper.selectOne(any())).thenReturn(user(2L, "b@test.com", "B"));
        when(friendshipMapper.selectOne(any())).thenReturn(friendship(9L, 1L, 2L, "REJECTED"));
        doAnswer(inv -> {
            Friendship f = inv.getArgument(0);
            f.setId(11L);
            return 1;
        }).when(friendshipMapper).insert(any(Friendship.class));

        FriendRequestVO vo = friendService.sendRequest(1L, "b@test.com");

        assertThat(vo.getUserId()).isEqualTo(2L);
        verify(friendshipMapper).deleteById(9L);
        verify(friendshipMapper).insert(any(Friendship.class));
    }

    // ---- acceptRequest ----

    @Test
    void acceptRequest_setsAccepted() {
        when(friendshipMapper.selectById(5L)).thenReturn(friendship(5L, 2L, 1L, "PENDING"));
        when(userMapper.selectById(2L)).thenReturn(user(2L, "b@test.com", "B"));

        FriendVO vo = friendService.acceptRequest(1L, 5L);

        assertThat(vo.getId()).isEqualTo(2L);
        assertThat(vo.getNickname()).isEqualTo("B");
        verify(friendshipMapper).updateById(any(Friendship.class));
    }

    @Test
    void acceptRequest_notFoundThrows404() {
        when(friendshipMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> friendService.acceptRequest(1L, 99L))
                .isInstanceOfSatisfying(BizException.class, e -> assertThat(e.getCode()).isEqualTo(404));
    }

    @Test
    void acceptRequest_notAddresseeThrows404() {
        when(friendshipMapper.selectById(5L)).thenReturn(friendship(5L, 2L, 99L, "PENDING"));

        assertThatThrownBy(() -> friendService.acceptRequest(1L, 5L))
                .isInstanceOfSatisfying(BizException.class, e -> assertThat(e.getCode()).isEqualTo(404));
    }

    @Test
    void acceptRequest_alreadyProcessedThrows400() {
        when(friendshipMapper.selectById(5L)).thenReturn(friendship(5L, 2L, 1L, "ACCEPTED"));

        assertThatThrownBy(() -> friendService.acceptRequest(1L, 5L))
                .isInstanceOfSatisfying(BizException.class, e -> assertThat(e.getCode()).isEqualTo(400));
    }

    // ---- rejectRequest ----

    @Test
    void rejectRequest_setsRejected() {
        when(friendshipMapper.selectById(5L)).thenReturn(friendship(5L, 2L, 1L, "PENDING"));

        friendService.rejectRequest(1L, 5L);

        verify(friendshipMapper).updateById(any(Friendship.class));
    }

    // ---- removeFriend ----

    @Test
    void removeFriend_deletesAccepted() {
        when(friendshipMapper.selectOne(any())).thenReturn(friendship(5L, 2L, 1L, "ACCEPTED"));

        friendService.removeFriend(1L, 2L);

        verify(friendshipMapper).deleteById(5L);
    }

    @Test
    void removeFriend_notFriendThrows404() {
        when(friendshipMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> friendService.removeFriend(1L, 2L))
                .isInstanceOfSatisfying(BizException.class, e -> assertThat(e.getCode()).isEqualTo(404));
    }

    // ---- isFriend ----

    @Test
    void isFriend_acceptedReturnsTrue() {
        when(friendshipMapper.selectOne(any())).thenReturn(friendship(5L, 2L, 1L, "ACCEPTED"));

        assertThat(friendService.isFriend(1L, 2L)).isTrue();
    }

    @Test
    void isFriend_pendingReturnsFalse() {
        when(friendshipMapper.selectOne(any())).thenReturn(friendship(5L, 2L, 1L, "PENDING"));

        assertThat(friendService.isFriend(1L, 2L)).isFalse();
    }

    @Test
    void isFriend_noneReturnsFalse() {
        when(friendshipMapper.selectOne(any())).thenReturn(null);

        assertThat(friendService.isFriend(1L, 2L)).isFalse();
    }

    // ---- listFriends ----

    @Test
    void listFriends_returnsBothDirections() {
        when(friendshipMapper.selectList(any())).thenReturn(List.of(
                friendship(1L, 1L, 2L, "ACCEPTED"),
                friendship(2L, 3L, 1L, "ACCEPTED")));
        when(userMapper.selectByIds(any())).thenReturn(List.of(
                user(2L, "b@test.com", "B"), user(3L, "c@test.com", "C")));

        List<FriendVO> list = friendService.listFriends(1L);

        assertThat(list).hasSize(2);
        assertThat(list).extracting(FriendVO::getId).containsExactlyInAnyOrder(2L, 3L);
    }

    // ---- listRequests ----

    @Test
    void listRequests_splitsReceivedAndSent() {
        when(friendshipMapper.selectList(any())).thenReturn(
                List.of(friendship(1L, 2L, 1L, "PENDING")),
                List.of(friendship(2L, 1L, 3L, "PENDING")));
        when(userMapper.selectByIds(any())).thenReturn(List.of(
                user(2L, "b@test.com", "B"), user(3L, "c@test.com", "C")));

        FriendRequestsVO vo = friendService.listRequests(1L);

        assertThat(vo.getReceived()).hasSize(1);
        assertThat(vo.getReceived().get(0).getUserId()).isEqualTo(2L);
        assertThat(vo.getSent()).hasSize(1);
        assertThat(vo.getSent().get(0).getUserId()).isEqualTo(3L);
    }

    // ---- searchUser ----

    @Test
    void searchUser_returnsVO() {
        when(userMapper.selectOne(any())).thenReturn(user(2L, "b@test.com", "B"));

        UserVO vo = friendService.searchUser(1L, "b@test.com");

        assertThat(vo.getId()).isEqualTo(2L);
        assertThat(vo.getNickname()).isEqualTo("B");
    }

    @Test
    void searchUser_notFoundThrows404() {
        when(userMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> friendService.searchUser(1L, "nobody@test.com"))
                .isInstanceOfSatisfying(BizException.class, e -> assertThat(e.getCode()).isEqualTo(404));
    }

    // ---- createInvite ----

    @Test
    void createInvite_generatesTokenAndUrl() {
        doAnswer(inv -> {
            FriendInvite i = inv.getArgument(0);
            i.setId(1L);
            return 1;
        }).when(friendInviteMapper).insert(any(FriendInvite.class));

        InviteVO vo = friendService.createInvite(1L);

        assertThat(vo.getToken()).isNotBlank();
        assertThat(vo.getUrl()).startsWith("/invite/").endsWith(vo.getToken());
        assertThat(vo.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    // ---- acceptInvite ----

    @Test
    void acceptInvite_createsRequest() {
        when(friendInviteMapper.selectOne(any())).thenReturn(
                invite(2L, "tok", LocalDateTime.now().plusDays(1)));
        when(friendshipMapper.selectOne(any())).thenReturn(null);
        doAnswer(inv -> {
            Friendship f = inv.getArgument(0);
            f.setId(20L);
            return 1;
        }).when(friendshipMapper).insert(any(Friendship.class));

        friendService.acceptInvite(1L, "tok");

        verify(friendshipMapper).insert(any(Friendship.class));
    }

    @Test
    void acceptInvite_invalidThrows404() {
        when(friendInviteMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> friendService.acceptInvite(1L, "bad"))
                .isInstanceOfSatisfying(BizException.class, e -> assertThat(e.getCode()).isEqualTo(404));
    }

    @Test
    void acceptInvite_expiredThrows400() {
        when(friendInviteMapper.selectOne(any())).thenReturn(
                invite(2L, "tok", LocalDateTime.now().minusDays(1)));

        assertThatThrownBy(() -> friendService.acceptInvite(1L, "tok"))
                .isInstanceOfSatisfying(BizException.class, e -> assertThat(e.getCode()).isEqualTo(400));
    }

    @Test
    void acceptInvite_selfThrows400() {
        when(friendInviteMapper.selectOne(any())).thenReturn(
                invite(1L, "tok", LocalDateTime.now().plusDays(1)));

        assertThatThrownBy(() -> friendService.acceptInvite(1L, "tok"))
                .isInstanceOfSatisfying(BizException.class, e -> assertThat(e.getCode()).isEqualTo(400));
    }
}
