package com.example.score.common;

import jakarta.servlet.http.HttpSession;

/**
 * session 读取工具。
 */
public final class SessionUtil {

    private SessionUtil() {
    }

    public static LoginUser get(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(LoginUser.SESSION_KEY);
        return value instanceof LoginUser user ? user : null;
    }

    /** 取当前登录用户，未登录视为异常（拦截器已兜底）。 */
    public static LoginUser require(HttpSession session) {
        LoginUser user = get(session);
        if (user == null) {
            throw new BizException("登录状态已失效，请重新登录。");
        }
        return user;
    }
}
