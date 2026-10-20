package com.example.score.common;

/**
 * 学生账号初始密码与改密规则：8~20 位、不含空格、同时含字母与数字，且不能等于初始密码。
 */
public final class PasswordPolicy {

    /** 教师新建学生账号的初始密码 */
    public static final String INITIAL_PASSWORD = "114514";

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 20;

    private PasswordPolicy() {
    }

    /**
     * 校验新密码。
     *
     * @return 错误提示；符合要求时返回 null
     */
    public static String validate(String password) {
        if (password == null || password.isEmpty()) {
            return "请输入新密码。";
        }
        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            return "密码长度需为 " + MIN_LENGTH + "~" + MAX_LENGTH + " 位。";
        }
        if (password.matches(".*\\s.*")) {
            return "密码不能包含空格。";
        }
        boolean hasLetter = password.matches(".*[A-Za-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        if (!hasLetter || !hasDigit) {
            return "密码必须同时包含字母和数字。";
        }
        if (INITIAL_PASSWORD.equals(password)) {
            return "新密码不能与初始密码相同。";
        }
        return null;
    }
}
