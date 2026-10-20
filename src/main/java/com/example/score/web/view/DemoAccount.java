package com.example.score.web.view;

/** 登录页演示账号按钮的数据（班长由随机或教师指定）。 */
public record DemoAccount(String username, String name, boolean monitor) {

    public String roleLabel() {
        return monitor ? "班长" : "学生";
    }
}
