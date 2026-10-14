package com.cloudrecord.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudrecord.common.BizException;
import com.cloudrecord.entity.EmailCode;
import com.cloudrecord.mapper.EmailCodeMapper;
import com.cloudrecord.service.EmailCodeService;
import com.cloudrecord.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmailCodeServiceImpl implements EmailCodeService {

    private static final long RESEND_INTERVAL_SECONDS = 60;
    private static final int CODE_EXPIRE_MINUTES = 10;

    private final EmailCodeMapper emailCodeMapper;
    private final MailService mailService;
    private final SecureRandom random = new SecureRandom();

    @Override
    public void send(String email, String purpose) {
        EmailCode latest = emailCodeMapper.selectOne(new LambdaQueryWrapper<EmailCode>()
                .eq(EmailCode::getEmail, email)
                .orderByDesc(EmailCode::getCreatedAt)
                .last("LIMIT 1"));
        if (latest != null && latest.getCreatedAt() != null
                && Duration.between(latest.getCreatedAt(), LocalDateTime.now()).getSeconds() < RESEND_INTERVAL_SECONDS) {
            throw BizException.tooManyRequests("发送太频繁，请稍后再试");
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        EmailCode entity = new EmailCode();
        entity.setEmail(email);
        entity.setCode(code);
        entity.setPurpose(purpose);
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES));
        emailCodeMapper.insert(entity);
        mailService.sendCode(email, code);
    }

    @Override
    public void verify(String email, String code, String purpose) {
        EmailCode latest = emailCodeMapper.selectOne(new LambdaQueryWrapper<EmailCode>()
                .eq(EmailCode::getEmail, email)
                .eq(EmailCode::getPurpose, purpose)
                .orderByDesc(EmailCode::getCreatedAt)
                .last("LIMIT 1"));
        if (latest == null || !Objects.equals(latest.getCode(), code)) {
            throw BizException.badRequest("验证码错误");
        }
        if (latest.getExpiresAt() != null && latest.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw BizException.badRequest("验证码已过期，请重新获取");
        }
    }
}
