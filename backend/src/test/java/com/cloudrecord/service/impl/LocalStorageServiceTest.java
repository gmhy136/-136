package com.cloudrecord.service.impl;

import com.cloudrecord.common.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalStorageService service() {
        return new LocalStorageService(tempDir.toString());
    }

    @Test
    void store_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "abc".getBytes());

        String url = service().store(file);

        assertThat(url).startsWith("/uploads/").endsWith(".png");
        assertThat(Files.list(tempDir).count()).isEqualTo(1);
    }

    @Test
    void store_emptyFileThrows400() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> service().store(file))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(400));
    }

    @Test
    void store_oversizeThrows400() {
        MockMultipartFile file = new MockMultipartFile("file", "big.png", "image/png",
                new byte[5 * 1024 * 1024 + 1]);

        assertThatThrownBy(() -> service().store(file))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(400));
    }

    @Test
    void store_badExtensionThrows400() {
        MockMultipartFile file = new MockMultipartFile("file", "evil.txt", "text/plain", "abc".getBytes());

        assertThatThrownBy(() -> service().store(file))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getCode()).isEqualTo(400));
    }
}
