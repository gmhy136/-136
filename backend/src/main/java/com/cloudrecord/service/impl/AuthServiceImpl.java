package com.cloudrecord.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudrecord.common.BizException;
import com.cloudrecord.dto.LoginRequest;
import com.cloudrecord.dto.RegisterRequest;
import com.cloudrecord.entity.User;
import com.cloudrecord.mapper.UserMapper;
import com.cloudrecord.security.JwtService;
import com.cloudrecord.service.AuthService;
import com.cloudrecord.service.EmailCodeService;
import com.cloudrecord.vo.AuthResponse;
import com.cloudrecord.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String PURPOSE_REGISTER = "REGISTER";

    private final UserMapper userMapper;
    private final EmailCodeService emailCodeService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse register(RegisterRequest request) {
        emailCodeService.verify(request.getEmail(), request.getCode(), PURPOSE_REGISTER);

        Long exists = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()));
        if (exists != null && exists > 0) {
            throw BizException.badRequest("该邮箱已注册，可直接登录");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userMapper.insert(user);
        return buildAuth(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BizException.badRequest("邮箱或密码错误");
        }
        return buildAuth(user);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        Long userId;
        try {
            if (!jwtService.isRefreshToken(refreshToken)) {
                throw BizException.unauthorized("登录已过期，请重新登录");
            }
            userId = jwtService.parseUserId(refreshToken);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw BizException.unauthorized("登录已过期，请重新登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BizException.unauthorized("登录已过期，请重新登录");
        }
        return buildAuth(user);
    }

    private AuthResponse buildAuth(User user) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user.getId()))
                .refreshToken(jwtService.generateRefreshToken(user.getId()))
                .user(UserVO.from(user))
                .build();
    }
}
