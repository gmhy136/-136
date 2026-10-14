package com.cloudrecord.service.impl;

import com.cloudrecord.common.BizException;
import com.cloudrecord.common.ErrorCode;
import com.cloudrecord.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * 本地磁盘存储实现：校验类型/大小后写入 uploads/ 随机文件名。
 */
@Service
public class LocalStorageService implements StorageService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_SIZE = 5L * 1024 * 1024;

    private final Path uploadDir;

    public LocalStorageService(@Value("${app.upload-dir:uploads}") String dir) {
        this.uploadDir = Paths.get(dir).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BizException.badRequest("请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw BizException.badRequest("图片不能超过 5MB");
        }
        String ext = extOf(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext)) {
            throw BizException.badRequest("仅支持 jpg/png/webp 格式");
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            Files.createDirectories(uploadDir);
            file.transferTo(uploadDir.resolve(filename));
        } catch (IOException e) {
            throw new BizException(ErrorCode.SERVER_ERROR, "图片保存失败，请稍后再试");
        }
        return "/uploads/" + filename;
    }

    private String extOf(String name) {
        if (name == null) {
            return "";
        }
        int i = name.lastIndexOf('.');
        return i < 0 ? "" : name.substring(i + 1).toLowerCase();
    }
}
