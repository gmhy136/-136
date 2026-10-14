package com.cloudrecord.service;

import com.cloudrecord.dto.LoginRequest;
import com.cloudrecord.dto.RegisterRequest;
import com.cloudrecord.vo.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshToken);
}
