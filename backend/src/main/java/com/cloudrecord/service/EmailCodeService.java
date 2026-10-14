package com.cloudrecord.service;

public interface EmailCodeService {

    /** 生成并发送验证码（60s 频控）。 */
    void send(String email, String purpose);

    /** 校验验证码，错误/过期抛 BizException。 */
    void verify(String email, String code, String purpose);
}
