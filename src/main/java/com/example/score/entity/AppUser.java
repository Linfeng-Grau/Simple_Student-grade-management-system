package com.example.score.entity;

import com.example.score.common.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 登录账号：教师、学生、班长共用，用 role 区分。演示项目密码明文保存。
 */
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 40)
    private String username;

    @Column(name = "password", nullable = false, length = 60)
    private String password;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    /** 关联编号：教师=工号，学生/班长=学号 */
    @Column(name = "ref_no", nullable = false, length = 20)
    private String refNo;

    /** 学生/班长所属班级，教师为空 */
    @Column(name = "class_no", length = 20)
    private String classNo;

    /** 首次登录是否必须修改密码（教师新建的学生账号为 true） */
    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;

    /** 是否为登录页提供的演示账号 */
    @Column(name = "demo_account", nullable = false)
    private boolean demoAccount;

    protected AppUser() {
    }

    public AppUser(String username, String password, String name, Role role, String refNo, String classNo) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.refNo = refNo;
        this.classNo = classNo;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
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

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public boolean isDemoAccount() {
        return demoAccount;
    }

    public void setDemoAccount(boolean demoAccount) {
        this.demoAccount = demoAccount;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
