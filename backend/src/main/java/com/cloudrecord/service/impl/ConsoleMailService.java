package com.cloudrecord.service.impl;

import com.cloudrecord.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * MVP 邮件实现：控制台打印验证码，无 SMTP 环境也能跑通全流程。
 */
@Slf4j
@Service
public class ConsoleMailService implements MailService {

    @Override
    public void sendCode(String email, String code) {
        log.info("[云记录] 验证码已发送到 {}：{}", email, code);
    }
}
