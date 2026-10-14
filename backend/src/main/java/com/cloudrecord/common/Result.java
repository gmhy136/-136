package com.cloudrecord.common;

import lombok.Data;

/**
 * 统一响应体 { code, message, data }，code=0 表示成功。
 */
@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(ErrorCode.SUCCESS, "ok", data);
    }

    public static Result<Void> ok() {
        return new Result<>(ErrorCode.SUCCESS, "ok", null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
