package com.cloudrecord.service.impl;

import com.cloudrecord.common.BizException;
import com.cloudrecord.dto.LoginRequest;
import com.cloudrecord.dto.RegisterRequest;
import com.cloudrecord.entity.User;
import com.cloudrecord.mapper.UserMapper;
import com.cloudrecord.security.JwtService;
import com.cloudrecord.service.EmailCodeService;
import com.cloudrecord.vo.AuthResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private EmailCodeService emailCodeService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest() {
        RegisterRequest r = new RegisterRequest();
        r.setEmail("a@b.com");
        r.setCode("123456");
        r.setNickname("小明");
        r.setPassword("secret1");
        return r;
    }

    private User user() {
        User u = new User();
        u.setId(1L);
        u.setEmail("a@b.com");
        u.setNickname("小明");
        u.setPassword("enc");
        return u;
    }

    @Test
    void register_success() {
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("secret1")).thenReturn("enc");
        when(jwtService.generateAccessToken(1L)).thenReturn("access");
        when(jwtService.generateRefreshToken(1L)).thenReturn("refresh");
        // 模拟 insert 后回填自增主键
        doAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return 1;
        }).when(userMapper).insert(any(User.class));

        AuthResponse resp = authService.register(registerRequest());

        assertThat(resp.getAccessToken()).isEqualTo("access");
        assertThat(resp.getRefreshToken()).isEqualTo("refresh");
        assertThat(resp.getUser().getEmail()).isEqualTo("a@b.com");
        verify(emailCodeService).verify("a@b.com", "123456", "REGISTER");
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void register_duplicateEmailThrows() {
        when(userMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(400));
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void login_success() {
        when(userMapper.selectOne(any())).thenReturn(user());
        when(passwordEncoder.matches("secret1", "enc")).thenReturn(true);
        when(jwtService.generateAccessToken(1L)).thenReturn("access");
        when(jwtService.generateRefreshToken(1L)).thenReturn("refresh");

        LoginRequest req = new LoginRequest();
        req.setEmail("a@b.com");
        req.setPassword("secret1");
        AuthResponse resp = authService.login(req);

        assertThat(resp.getAccessToken()).isEqualTo("access");
        assertThat(resp.getUser().getId()).isEqualTo(1L);
    }

    @Test
    void login_wrongPasswordThrows() {
        when(userMapper.selectOne(any())).thenReturn(user());
        when(passwordEncoder.matches("bad", "enc")).thenReturn(false);

        LoginRequest req = new LoginRequest();
        req.setEmail("a@b.com");
        req.setPassword("bad");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(400));
    }

    @Test
    void login_unknownEmailThrows() {
        when(userMapper.selectOne(any())).thenReturn(null);

        LoginRequest req = new LoginRequest();
        req.setEmail("no@b.com");
        req.setPassword("x");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(400));
    }

    @Test
    void refresh_success() {
        when(jwtService.isRefreshToken("refresh")).thenReturn(true);
        when(jwtService.parseUserId("refresh")).thenReturn(1L);
        when(userMapper.selectById(1L)).thenReturn(user());
        when(jwtService.generateAccessToken(1L)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(1L)).thenReturn("new-refresh");

        AuthResponse resp = authService.refresh("refresh");

        assertThat(resp.getAccessToken()).isEqualTo("new-access");
        assertThat(resp.getRefreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void refresh_accessTokenRejected() {
        when(jwtService.isRefreshToken("access")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh("access"))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(401));
    }

    @Test
    void refresh_invalidTokenThrows() {
        when(jwtService.isRefreshToken("bad")).thenThrow(new RuntimeException("expired"));

        assertThatThrownBy(() -> authService.refresh("bad"))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(401));
    }

    @Test
    void refresh_userNotFoundThrows() {
        when(jwtService.isRefreshToken("refresh")).thenReturn(true);
        when(jwtService.parseUserId("refresh")).thenReturn(999L);
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> authService.refresh("refresh"))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(401));
    }
}
