package com.example.score.web.view;

import java.math.BigDecimal;

/** 学生成绩单中的一行（未录入的课程分数为 null）。 */
public record StudentScoreView(
        String cno,
        String cname,
        BigDecimal credit,
        int usualRatio,
        int examRatio,
        BigDecimal usualScore,
        BigDecimal examScore,
        BigDecimal totalScore,
        BigDecimal gradePoint) {
}
