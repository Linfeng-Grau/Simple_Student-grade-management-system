package com.example.score.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 成绩计算器：全系统唯一的总评与绩点口径。
 * 总评 = 平时 × 比例 + 考试 × 比例；绩点 =（总评 - 50）/ 10，总评 < 60 记 0；跨课程汇总按学分加权。
 * 后端保存时用它重算，前端 JS 只做预览。
 */
public final class GradeCalculator {

    /** 及格线 */
    public static final BigDecimal PASS_LINE = new BigDecimal("60");

    private GradeCalculator() {
    }

    /** 总评；平时或考试任一为空时返回 null（未完整录入）。 */
    public static BigDecimal computeTotal(BigDecimal usualScore, BigDecimal examScore, int usualRatio) {
        if (usualScore == null || examScore == null) {
            return null;
        }
        BigDecimal ratio = BigDecimal.valueOf(usualRatio);
        BigDecimal rest = BigDecimal.valueOf(100 - usualRatio);
        return usualScore.multiply(ratio)
                .add(examScore.multiply(rest))
                .divide(BigDecimal.valueOf(100), 1, RoundingMode.HALF_UP);
    }

    /** 单科绩点：(总评 - 50) / 10，低于 60 记 0，保留 2 位小数。 */
    public static BigDecimal gradePoint(BigDecimal totalScore) {
        if (totalScore == null) {
            return null;
        }
        if (totalScore.compareTo(PASS_LINE) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return totalScore.subtract(new BigDecimal("50"))
                .divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
    }

    /**
     * 按学分加权的平均值；列表为空、长度不一致或学分总和为 0 时返回 null。
     *
     * @param values  每门课的值（总评或绩点）
     * @param credits 对应的学分
     */
    public static BigDecimal weightedAverage(List<BigDecimal> values, List<BigDecimal> credits) {
        if (values == null || credits == null || values.isEmpty() || values.size() != credits.size()) {
            return null;
        }
        BigDecimal weightedSum = BigDecimal.ZERO;
        BigDecimal creditSum = BigDecimal.ZERO;
        for (int i = 0; i < values.size(); i++) {
            BigDecimal value = values.get(i);
            BigDecimal credit = credits.get(i);
            if (value == null || credit == null) {
                continue;
            }
            weightedSum = weightedSum.add(value.multiply(credit));
            creditSum = creditSum.add(credit);
        }
        if (creditSum.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return weightedSum.divide(creditSum, 2, RoundingMode.HALF_UP);
    }

    /** 算术平均（忽略空值），没有有效值时返回 null */
    public static BigDecimal average(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (BigDecimal value : values) {
            if (value == null) {
                continue;
            }
            sum = sum.add(value);
            count++;
        }
        if (count == 0) {
            return null;
        }
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    /** 最大值，空列表返回 null */
    public static BigDecimal max(List<BigDecimal> values) {
        BigDecimal max = null;
        if (values != null) {
            for (BigDecimal value : values) {
                if (value != null && (max == null || value.compareTo(max) > 0)) {
                    max = value;
                }
            }
        }
        return max;
    }

    /** 最小值，空列表返回 null */
    public static BigDecimal min(List<BigDecimal> values) {
        BigDecimal min = null;
        if (values != null) {
            for (BigDecimal value : values) {
                if (value != null && (min == null || value.compareTo(min) < 0)) {
                    min = value;
                }
            }
        }
        return min;
    }

    /** 分数是否合法（0 ~ 100） */
    public static boolean isValidScore(BigDecimal score) {
        return score != null
                && score.compareTo(BigDecimal.ZERO) >= 0
                && score.compareTo(new BigDecimal("100")) <= 0;
    }
}
