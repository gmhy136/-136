package com.cloudrecord.common;

/**
 * 统一错误码（与方案设计文档 §12 一致）。
 */
public final class ErrorCode {

    public static final int SUCCESS = 0;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int TOO_MANY_REQUESTS = 429;
    public static final int SERVER_ERROR = 500;

    private ErrorCode() {
    }
}
