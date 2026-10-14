package com.cloudrecord.controller;

import com.cloudrecord.common.Result;
import com.cloudrecord.dto.LoginRequest;
import com.cloudrecord.dto.RefreshRequest;
import com.cloudrecord.dto.RegisterRequest;
import com.cloudrecord.dto.SendCodeRequest;
import com.cloudrecord.service.AuthService;
import com.cloudrecord.service.EmailCodeService;
import com.cloudrecord.vo.AuthResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EmailCodeService emailCodeService;
    private final AuthService authService;

    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        emailCodeService.send(request.getEmail(), request.getPurpose());
        return Result.ok();
    }

    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public Result<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return Result.ok(authService.refresh(request.getRefreshToken()));
    }
}
