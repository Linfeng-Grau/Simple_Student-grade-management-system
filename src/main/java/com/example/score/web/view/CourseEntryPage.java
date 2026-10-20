package com.example.score.web.view;

import java.math.BigDecimal;
import java.util.List;

/** 录入页所需的全部数据。 */
public record CourseEntryPage(
        String cno,
        String cname,
        BigDecimal credit,
        String classNo,
        String className,
        int usualRatio,
        int examRatio,
        List<ScoreRowView> rows,
        int enteredCount,
        BigDecimal avgTotal,
        BigDecimal maxTotal,
        BigDecimal minTotal,
        BigDecimal passRate) {
}
