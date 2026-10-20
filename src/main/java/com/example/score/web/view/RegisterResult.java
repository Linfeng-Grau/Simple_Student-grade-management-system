package com.example.score.web.view;

import java.util.List;

/**
 * 登记新学生的结果。
 *
 * @param errors        错误提示；为空表示登记成功
 * @param duplicateName 同班已存在同名同学（登记成功但提示老师核对，避免重复录入）
 */
public record RegisterResult(List<String> errors, boolean duplicateName) {
}
