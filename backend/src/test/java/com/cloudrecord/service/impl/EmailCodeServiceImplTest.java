package com.cloudrecord.service.impl;

import com.cloudrecord.common.BizException;
import com.cloudrecord.entity.EmailCode;
import com.cloudrecord.mapper.EmailCodeMapper;
import com.cloudrecord.service.MailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailCodeServiceImplTest {

    @Mock
    private EmailCodeMapper emailCodeMapper;

    @Mock
    private MailService mailService;

    @InjectMocks
    private EmailCodeServiceImpl service;

    @Test
    void send_generatesCodeAndMails() {
        when(emailCodeMapper.selectOne(any())).thenReturn(null);

        service.send("a@b.com", "REGISTER");

        ArgumentCaptor<EmailCode> captor = ArgumentCaptor.forClass(EmailCode.class);
        verify(emailCodeMapper).insert(captor.capture());
        EmailCode saved = captor.getValue();
        assertThat(saved.getCode()).matches("\\d{6}");
        assertThat(saved.getEmail()).isEqualTo("a@b.com");
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(9));
        verify(mailService).sendCode("a@b.com", saved.getCode());
    }

    @Test
    void send_isRateLimitedWithin60Seconds() {
        EmailCode recent = new EmailCode();
        recent.setCreatedAt(LocalDateTime.now().minusSeconds(30));
        when(emailCodeMapper.selectOne(any())).thenReturn(recent);

        assertThatThrownBy(() -> service.send("a@b.com", "REGISTER"))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(429));
        verify(emailCodeMapper, never()).insert(any(EmailCode.class));
    }

    @Test
    void send_allowedAfter60Seconds() {
        EmailCode old = new EmailCode();
        old.setCreatedAt(LocalDateTime.now().minusSeconds(61));
        when(emailCodeMapper.selectOne(any())).thenReturn(old);

        service.send("a@b.com", "REGISTER");

        verify(emailCodeMapper).insert(any(EmailCode.class));
    }

    @Test
    void verify_success() {
        EmailCode latest = new EmailCode();
        latest.setCode("123456");
        latest.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        when(emailCodeMapper.selectOne(any())).thenReturn(latest);

        assertThatCode(() -> service.verify("a@b.com", "123456", "REGISTER"))
                .doesNotThrowAnyException();
    }

    @Test
    void verify_wrongCodeThrows() {
        EmailCode latest = new EmailCode();
        latest.setCode("123456");
        latest.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        when(emailCodeMapper.selectOne(any())).thenReturn(latest);

        assertThatThrownBy(() -> service.verify("a@b.com", "000000", "REGISTER"))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(400));
    }

    @Test
    void verify_expiredThrows() {
        EmailCode latest = new EmailCode();
        latest.setCode("123456");
        latest.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(emailCodeMapper.selectOne(any())).thenReturn(latest);

        assertThatThrownBy(() -> service.verify("a@b.com", "123456", "REGISTER"))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(400));
    }

    @Test
    void verify_noCodeThrows() {
        when(emailCodeMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> service.verify("a@b.com", "123456", "REGISTER"))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(400));
    }
}
