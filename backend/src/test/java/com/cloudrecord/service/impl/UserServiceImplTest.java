package com.cloudrecord.service.impl;

import com.cloudrecord.common.BizException;
import com.cloudrecord.dto.UpdateProfileRequest;
import com.cloudrecord.entity.User;
import com.cloudrecord.mapper.UserMapper;
import com.cloudrecord.vo.UserVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user() {
        User u = new User();
        u.setId(1L);
        u.setEmail("a@b.com");
        u.setNickname("旧名");
        u.setBio("旧简介");
        return u;
    }

    @Test
    void me_returnsUser() {
        when(userMapper.selectById(1L)).thenReturn(user());

        UserVO vo = userService.me(1L);

        assertThat(vo.getNickname()).isEqualTo("旧名");
        assertThat(vo.getEmail()).isEqualTo("a@b.com");
    }

    @Test
    void me_notFoundThrows() {
        when(userMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> userService.me(1L))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(404));
    }

    @Test
    void updateProfile_updatesFieldsAndClearsBlank() {
        when(userMapper.selectById(1L)).thenReturn(user());

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setNickname("新名");
        req.setBio("   ");

        UserVO vo = userService.updateProfile(1L, req);

        assertThat(vo.getNickname()).isEqualTo("新名");
        assertThat(vo.getBio()).isNull();
        verify(userMapper).updateById(any(User.class));
    }
}
