package com.cloudrecord.security;

import com.cloudrecord.common.BizException;
import com.cloudrecord.common.ErrorCode;

/**
 * 当前登录用户上下文（ThreadLocal）。
 */
public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId) {
        USER_ID.set(userId);
    }

    public static Long get() {
        return USER_ID.get();
    }

    public static Long requireUserId() {
        Long userId = USER_ID.get();
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "请先登录");
        }
        return userId;
    }

    public static void clear() {
        USER_ID.remove();
    }
}
