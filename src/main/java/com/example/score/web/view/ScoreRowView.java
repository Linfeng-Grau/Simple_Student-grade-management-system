package com.example.score.web.view;

import java.math.BigDecimal;

/** 录入表格中的一行。 */
public record ScoreRowView(
        String sno,
        String sname,
        BigDecimal usualScore,
        BigDecimal examScore,
        BigDecimal totalScore) {
}
