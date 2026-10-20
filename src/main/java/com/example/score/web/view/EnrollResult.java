package com.example.score.web.view;

import java.util.List;

/**
 * 把已登记学生加入课程的结果。
 *
 * @param errors            错误提示；为空表示成功
 * @param added             实际新增的选课数量（人次）
 * @param skippedOtherClass 因为「不是本班课程」而被跳过的学生数（仅「我的全部课程」会出现）
 * @param alreadyEnrolled   已经在这门课里、被忽略的学生数
 */
public record EnrollResult(List<String> errors, int added, int skippedOtherClass, int alreadyEnrolled) {
}
