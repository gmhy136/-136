package com.cloudrecord.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    private final JwtService jwtService = new JwtService(SECRET, 7200000, 1209600000);

    @Test
    void generateAndParseAccessToken() {
        String token = jwtService.generateAccessToken(42L);

        assertThat(jwtService.parseUserId(token)).isEqualTo(42L);
        assertThat(jwtService.isRefreshToken(token)).isFalse();
    }

    @Test
    void refreshTokenIsMarkedAsRefresh() {
        String token = jwtService.generateRefreshToken(42L);

        assertThat(jwtService.parseUserId(token)).isEqualTo(42L);
        assertThat(jwtService.isRefreshToken(token)).isTrue();
    }

    @Test
    void parseInvalidTokenThrows() {
        assertThatThrownBy(() -> jwtService.parseUserId("not-a-jwt"))
                .isInstanceOf(Exception.class);
    }
}
