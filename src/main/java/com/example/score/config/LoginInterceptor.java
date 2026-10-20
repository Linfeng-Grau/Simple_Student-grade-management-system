package com.example.score.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.score.common.LoginUser;
import com.example.score.common.Role;
import com.example.score.common.SessionUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 登录与角色拦截：未登录跳登录页（记住原地址），角色不符跳「无权访问」；
 * 需首次改密的账号除 /password/change 外一律重定向到改密页。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        String contextPath = request.getContextPath();
        String path = request.getRequestURI().substring(contextPath.length());
        LoginUser user = SessionUtil.get(request.getSession(false));

        if (user == null) {
            String query = request.getQueryString();
            String target = query == null ? path : path + "?" + query;
            response.sendRedirect(contextPath + "/login?redirect="
                    + URLEncoder.encode(target, StandardCharsets.UTF_8));
            return false;
        }

        // 首次登录（教师新建账号）先强制修改密码，改完才能进业务页面
        if (user.isMustChangePassword() && !"/password/change".equals(path)) {
            response.sendRedirect(contextPath + "/password/change");
            return false;
        }

        Role required = requiredRole(path);
        if (required != null && user.getRole() != required) {
            response.sendRedirect(contextPath + "/forbidden");
            return false;
        }
        return true;
    }

    private static Role requiredRole(String path) {
        if (path.startsWith("/teacher")) {
            return Role.TEACHER;
        }
        if (path.startsWith("/student")) {
            return Role.STUDENT;
        }
        if (path.startsWith("/monitor")) {
            return Role.MONITOR;
        }
        return null;
    }
}
