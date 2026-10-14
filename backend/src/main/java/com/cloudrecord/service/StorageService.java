package com.cloudrecord.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储抽象（本地 / OSS 两实现，生产切换零改动业务代码）。
 */
public interface StorageService {

    /** 存储文件，返回可访问的相对 url（如 /uploads/xxx.jpg）。 */
    String store(MultipartFile file);
}
