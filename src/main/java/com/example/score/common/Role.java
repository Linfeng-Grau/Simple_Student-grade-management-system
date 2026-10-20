package com.example.score.common;

/**
 * 用户角色：自带中文名与登录后首页，菜单和拦截器都以此为准。
 */
public enum Role {

    TEACHER("教师", "/teacher/courses"),
    STUDENT("学生", "/student/scores"),
    MONITOR("班长", "/monitor/scores");

    private final String label;
    private final String homePath;

    Role(String label, String homePath) {
        this.label = label;
        this.homePath = homePath;
    }

    public String getLabel() {
        return label;
    }

    public String getHomePath() {
        return homePath;
    }

    /**
     * 角色对应的 URL 前缀，用于登录后回跳与越权判断。
     */
    public String getPathPrefix() {
        return "/" + name().toLowerCase();
    }
}
