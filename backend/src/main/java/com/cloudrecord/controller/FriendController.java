package com.cloudrecord.controller;

import com.cloudrecord.common.Result;
import com.cloudrecord.dto.SendFriendRequest;
import com.cloudrecord.security.UserContext;
import com.cloudrecord.service.FriendService;
import com.cloudrecord.vo.FriendRequestVO;
import com.cloudrecord.vo.FriendRequestsVO;
import com.cloudrecord.vo.FriendVO;
import com.cloudrecord.vo.InviteVO;
import com.cloudrecord.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping
    public Result<List<FriendVO>> list() {
        return Result.ok(friendService.listFriends(UserContext.requireUserId()));
    }

    @GetMapping("/requests")
    public Result<FriendRequestsVO> requests() {
        return Result.ok(friendService.listRequests(UserContext.requireUserId()));
    }

    @GetMapping("/search")
    public Result<UserVO> search(@RequestParam String email) {
        return Result.ok(friendService.searchUser(UserContext.requireUserId(), email));
    }

    @PostMapping("/requests")
    public Result<FriendRequestVO> send(@Valid @RequestBody SendFriendRequest request) {
        return Result.ok(friendService.sendRequest(UserContext.requireUserId(), request.getEmail()));
    }

    @PostMapping("/requests/{id}/accept")
    public Result<FriendVO> accept(@PathVariable Long id) {
        return Result.ok(friendService.acceptRequest(UserContext.requireUserId(), id));
    }

    @PostMapping("/requests/{id}/reject")
    public Result<Void> reject(@PathVariable Long id) {
        friendService.rejectRequest(UserContext.requireUserId(), id);
        return Result.ok();
    }

    @DeleteMapping("/{friendId}")
    public Result<Void> remove(@PathVariable Long friendId) {
        friendService.removeFriend(UserContext.requireUserId(), friendId);
        return Result.ok();
    }

    @PostMapping("/invites")
    public Result<InviteVO> createInvite() {
        return Result.ok(friendService.createInvite(UserContext.requireUserId()));
    }

    @PostMapping("/invites/{token}")
    public Result<Void> acceptInvite(@PathVariable String token) {
        friendService.acceptInvite(UserContext.requireUserId(), token);
        return Result.ok();
    }
}
