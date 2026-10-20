package com.example.score.web.view;

import java.math.BigDecimal;

/** 学生成绩单的汇总卡数据。 */
public record StudentSummary(
        int courseCount,
        int recordedCount,
        int failedCount,
        BigDecimal planCredits,
        BigDecimal earnedCredits,
        BigDecimal weightedAvg,
        BigDecimal weightedGpa) {
}
