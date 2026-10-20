package com.example.score.web.view;

import java.math.BigDecimal;
import java.util.List;

/** 班级排名页数据。 */
public record RankingView(
        String classNo,
        String className,
        int studentCount,
        int recordedCount,
        int passCount,
        BigDecimal classAvgGpa,
        BigDecimal passRate,
        List<RankingRow> rows) {
}
