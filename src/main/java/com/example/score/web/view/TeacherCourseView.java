package com.example.score.web.view;

import java.math.BigDecimal;

/** 教师端「我的课程」列表项。 */
public record TeacherCourseView(
        String cno,
        String cname,
        BigDecimal credit,
        String className,
        int studentCount,
        int enteredCount,
        BigDecimal avgTotal,
        int usualRatio,
        int examRatio) {

    /** 录入进度：已录人数 / 总人数 */
    public int percent() {
        return studentCount == 0 ? 0 : Math.round(enteredCount * 100f / studentCount);
    }
}
