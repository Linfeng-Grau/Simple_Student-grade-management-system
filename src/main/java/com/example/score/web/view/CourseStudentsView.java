package com.example.score.web.view;

import java.util.List;

/**
 * 教师端「学生管理」中，一门课程及其选课学生。
 *
 * @param students  已经选了这门课的学生
 * @param available 本班已登记、但还没选这门课的学生（可一键加入，天然满足「库里必须有学生信息」）
 */
public record CourseStudentsView(
        String cno,
        String cname,
        String className,
        List<StudentRow> students,
        List<StudentRow> available) {

    public int count() {
        return students.size();
    }

    public int availableCount() {
        return available.size();
    }
}
