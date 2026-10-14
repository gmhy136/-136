package com.cloudrecord.service;

public interface MailService {

    /** 发送验证码邮件。MVP 用控制台打印实现，生产切 SMTP。 */
    void sendCode(String email, String code);
}
