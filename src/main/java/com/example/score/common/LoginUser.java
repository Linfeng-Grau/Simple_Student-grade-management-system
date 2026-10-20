package com.example.score.common;

import java.io.Serializable;

/**
 * 登录后放进 session 的用户信息（不包含密码）。
 */
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** session 中保存登录用户的 key */
    public static final String SESSION_KEY = "loginUser";

    private final String username;
    private final String name;
    private final Role role;
    /** 关联编号：教师=工号，学生/班长=学号 */
    private final String refNo;
    /** 班级编号：学生/班长有值，教师为 null */
    private final String classNo;
    /** 首次登录是否必须修改密码 */
    private final boolean mustChangePassword;

    public LoginUser(String username, String name, Role role, String refNo, String classNo,
                     boolean mustChangePassword) {
        this.username = username;
        this.name = name;
        this.role = role;
        this.refNo = refNo;
        this.classNo = classNo;
        this.mustChangePassword = mustChangePassword;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public String getRefNo() {
        return refNo;
    }

    public String getClassNo() {
        return classNo;
    }

    public boolean isTeacher() {
        return role == Role.TEACHER;
    }

    public boolean isStudent() {
        return role == Role.STUDENT;
    }

    public boolean isMonitor() {
        return role == Role.MONITOR;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }
}
